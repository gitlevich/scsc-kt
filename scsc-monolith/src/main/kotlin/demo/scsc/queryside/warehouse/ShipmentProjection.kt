package demo.scsc.queryside.warehouse

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
class ShipmentProjection {

    @EventHandler
    fun on(event: ShipmentRequestedEvent, queryUpdateEmitter: QueryUpdateEmitter) {
        tx { em ->
            toEntities(event).forEach { product: ShipmentProduct ->
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
        tx { em ->
            val id = ShipmentProduct.Id(event.shipmentId, event.productId)
            val shipmentProduct = em.find(ShipmentProduct::class.java, id)
            em.remove(shipmentProduct)
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
    fun shippingRequests(): GetShippingQueryResponse = answer(GET_SHIPPING_REQUESTS) { em ->
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
        tx { it.createQuery("DELETE FROM ShipmentProduct").executeUpdate() }
    }

    private fun toEntities(event: ShipmentRequestedEvent): List<ShipmentProduct> =
        event.products.map { productId -> ShipmentProduct(ShipmentProduct.Id(event.shipmentId, productId)) }
            .toList()

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
