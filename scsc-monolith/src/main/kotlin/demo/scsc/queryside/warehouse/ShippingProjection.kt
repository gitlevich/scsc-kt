package demo.scsc.queryside.warehouse

import demo.scsc.Constants
import demo.scsc.api.Warehouse.GetShippingQueryResponse
import demo.scsc.api.Warehouse.GetShippingQueryResponse.ShippingItem
import demo.scsc.api.Warehouse.ProductAddedToPackageEvent
import demo.scsc.api.Warehouse.ShipmentRequestedEvent
import demo.scsc.config.JpaPersistenceUnit.Companion.forName
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
import java.util.function.Consumer
import java.util.stream.Collectors

@ProcessingGroup(Constants.PROCESSING_GROUP_WAREHOUSE)
class ShippingProjection {
    @EventHandler
    fun on(shipmentRequestedEvent: ShipmentRequestedEvent, queryUpdateEmitter: QueryUpdateEmitter) {
        val em = forName("SCSC")!!.newEntityManager
        em.transaction.begin()
        toEntities(shipmentRequestedEvent).forEach(Consumer<ShippingProductEntity> { entity: ShippingProductEntity ->
            em.persist(entity)
            updateSubscribers(
                entity.id!!.shippingId,
                entity.id!!.productId,
                queryUpdateEmitter
            )
        })
        em.transaction.commit()
        em.close()
    }

    @EventHandler
    fun on(productAddedToPackageEvent: ProductAddedToPackageEvent, queryUpdateEmitter: QueryUpdateEmitter) {
        val em = forName("SCSC")!!.newEntityManager
        em.transaction.begin()
        val id = ShippingProductEntity.Id(
            productAddedToPackageEvent.shipmentId,
            productAddedToPackageEvent.productId
        )
        val shippingProductEntity = em.find(ShippingProductEntity::class.java, id)
        em.remove(shippingProductEntity)
        em.transaction.commit()
        em.close()
        updateSubscribers(
            productAddedToPackageEvent.shipmentId,
            productAddedToPackageEvent.productId,
            true,
            queryUpdateEmitter
        )
    }

    private fun updateSubscribers(shipmentId: UUID, productId: UUID, queryUpdateEmitter: QueryUpdateEmitter) {
        updateSubscribers(shipmentId, productId, false, queryUpdateEmitter)
    }

    private fun updateSubscribers(
        shipmentId: UUID,
        productId: UUID,
        removed: Boolean,
        queryUpdateEmitter: QueryUpdateEmitter
    ) {
        queryUpdateEmitter.emit(
            { subscriptionQueryMessage: SubscriptionQueryMessage<*, *, ShippingItem?> -> GET_SHIPPING_REQUESTS == subscriptionQueryMessage.queryName },
            ShippingItem(
                shipmentId,
                productId,
                removed
            )
        )
    }

    @get:QueryHandler(queryName = GET_SHIPPING_REQUESTS)
    val shippingRequests: GetShippingQueryResponse
        get() {
            val em = forName("SCSC")!!.newEntityManager
            val shippingEntities = em
                .createQuery(
                    "SELECT s FROM ShippingProductEntity AS s ORDER BY s.id.shippingId",
                    ShippingProductEntity::class.java
                )
                .resultList
            val response = GetShippingQueryResponse(
                shippingEntities.stream()
                    .map { shippingProductEntity: ShippingProductEntity ->
                        ShippingItem(
                            shippingProductEntity.id!!.shippingId,
                            shippingProductEntity.id!!.productId,
                            false
                        )
                    }
                    .collect(Collectors.toList())
            )
            em.close()
            return response
        }

    @ResetHandler
    fun onReset() {
        val em = forName("SCSC")!!.newEntityManager
        em.transaction.begin()
        em.createQuery("DELETE FROM ShippingProductEntity").executeUpdate()
        em.transaction.commit()
    }

    private fun toEntities(shipmentRequestedEvent: ShipmentRequestedEvent): List<ShippingProductEntity> {
        return shipmentRequestedEvent.products.stream()
            .map { productId: UUID ->
                ShippingProductEntity(
                    ShippingProductEntity.Id(
                        shipmentRequestedEvent.shipmentId,
                        productId
                    )
                )
            }
            .toList()
    }

    @MessageHandlerInterceptor(messageType = EventMessage::class)
    fun intercept(
        message: EventMessage<*>,
        interceptorChain: InterceptorChain
    ) {
        LOG.info("[    EVENT ] " + message.payload.toString())
        interceptorChain.proceed()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ShippingProjection::class.java)
        const val GET_SHIPPING_REQUESTS = "warehouse:getShippingRequests"
    }
}
