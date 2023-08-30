package demo.scsc.queryside.warehouse

import com.typesafe.config.Config
import demo.scsc.Constants
import demo.scsc.api.warehouse.GetShippingQueryResponse
import demo.scsc.api.warehouse.GetShippingQueryResponse.ShippingItem
import demo.scsc.api.warehouse.ProductAddedToPackageEvent
import demo.scsc.api.warehouse.ShipmentRequestedEvent
import demo.scsc.util.answer
import demo.scsc.util.tx
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.EventMessage
import org.axonframework.eventhandling.ResetHandler
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.interceptors.MessageHandlerInterceptor
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.axonframework.queryhandling.SubscriptionQueryMessage
import org.slf4j.LoggerFactory
import java.util.*

@ProcessingGroup(Constants.PROCESSING_GROUP_WAREHOUSE)
class ShipmentProjection(private val appConfig: Config) {

    @EventHandler
    fun on(event: ShipmentRequestedEvent, queryUpdateEmitter: QueryUpdateEmitter) {
        tx(appConfig) { em ->
            event.toEntities().forEach { product: ShipmentProduct ->
                println("persisting $product")
                em.persist(product)
                updateSubscribers(
                    shipmentId = product.id.shippingId,
                    productId = product.id.productId,
                    queryUpdateEmitter = queryUpdateEmitter
                )
            }
        }
    }

    @EventHandler
    fun on(event: ProductAddedToPackageEvent, queryUpdateEmitter: QueryUpdateEmitter) {
        tx(appConfig) { em ->
            val id = ShipmentProduct.Id(event.shipmentId, event.productId)
            println("product added to package. Removing it: $id")
            em.find(ShipmentProduct::class.java, id)
                ?.let { em.remove(it) }
                ?: throw IllegalStateException("product $id not part of this shipment")
        }
        updateSubscribers(
            shipmentId = event.shipmentId,
            productId = event.productId,
            queryUpdateEmitter = queryUpdateEmitter,
            removed = true
        )
    }

    private fun updateSubscribers(
        shipmentId: UUID,
        productId: UUID,
        queryUpdateEmitter: QueryUpdateEmitter,
        removed: Boolean = false
    ) {
        queryUpdateEmitter.emit(
            { subscriptionQueryMessage: SubscriptionQueryMessage<*, *, ShippingItem> -> GET_SHIPPING_REQUESTS == subscriptionQueryMessage.queryName },
            ShippingItem(
                shipmentId = shipmentId,
                productId = productId,
                removed = removed
            )
        )
    }

    @QueryHandler(queryName = GET_SHIPPING_REQUESTS)
    fun shippingRequests(): GetShippingQueryResponse = answer(GET_SHIPPING_REQUESTS, appConfig) { em ->
        val shippingEntities = em.createQuery(
            "SELECT s FROM ShipmentProduct AS s ORDER BY s.id.shippingId",
            ShipmentProduct::class.java
        ).resultList

        GetShippingQueryResponse(
            shippingEntities
                .map { shipmentProduct: ShipmentProduct ->
                    ShippingItem(
                        shipmentId = shipmentProduct.id.shippingId,
                        productId = shipmentProduct.id.productId,
                        removed = false
                    )
                }
                .toList()
        )
    }

    @ResetHandler
    fun onReset() {
        tx(appConfig) { it.createQuery("DELETE FROM ShipmentProduct").executeUpdate() }
    }

    private fun ShipmentRequestedEvent.toEntities(): List<ShipmentProduct> =
        products.map { productId -> ShipmentProduct(ShipmentProduct.Id(shipmentId, productId)) }

    @MessageHandlerInterceptor(messageType = EventMessage::class)
    fun intercept(message: EventMessage<*>, interceptorChain: InterceptorChain) {
        LOG.info("[    EVENT ] " + message.payload.toString())
        interceptorChain.proceed()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ShipmentProjection::class.java)
        const val GET_SHIPPING_REQUESTS = "warehouse:getShippingRequests"
    }
}
