package demo.scsc.queryside.order

import demo.scsc.Constants
import demo.scsc.api.order.GetOrdersQuery
import demo.scsc.api.order.GetOrdersQueryResponse
import demo.scsc.api.order.GetOrdersQueryResponse.OrderLine
import demo.scsc.api.order.OrderCompletedEvent
import demo.scsc.api.order.OrderCreatedEvent
import demo.scsc.api.Payment.OrderFullyPaidEvent
import demo.scsc.api.Warehouse.PackageReadyEvent
import demo.scsc.config.JpaPersistenceUnit.Companion.forName
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.EventMessage
import org.axonframework.eventhandling.ResetHandler
import org.axonframework.eventhandling.replay.ResetContext
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.interceptors.MessageHandlerInterceptor
import org.axonframework.queryhandling.QueryHandler
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.util.stream.Collectors

@ProcessingGroup(Constants.PROCESSING_GROUP_ORDER)
class OrdersProjection {
    @EventHandler
    fun on(orderCreatedEvent: OrderCreatedEvent) {
        val em = forName("SCSC")!!.newEntityManager
        em.transaction.begin()
        em.persist(toEntity(orderCreatedEvent))
        em.transaction.commit()
        em.close()
    }

    @EventHandler
    fun on(orderFullyPaidEvent: OrderFullyPaidEvent) {
        val em = forName("SCSC")!!.newEntityManager
        em.transaction.begin()
        val orderEntity = em.find(OrderEntity::class.java, orderFullyPaidEvent.orderId)
        orderEntity.isPaid = true
        em.merge(orderEntity)
        em.transaction.commit()
        em.close()
    }

    @EventHandler
    fun on(packageReadyEvent: PackageReadyEvent) {
        val em = forName("SCSC")!!.newEntityManager
        em.transaction.begin()
        val orderEntity = em.find(OrderEntity::class.java, packageReadyEvent.orderId)
        orderEntity.isPrepared = true
        em.merge(orderEntity)
        em.transaction.commit()
        em.close()
    }

    @EventHandler
    fun on(orderCompletedEvent: OrderCompletedEvent) {
        val em = forName("SCSC")!!.newEntityManager
        em.transaction.begin()
        val orderEntity = em.find(OrderEntity::class.java, orderCompletedEvent.orderId)
        orderEntity.isReady = true
        em.merge(orderEntity)
        em.transaction.commit()
        em.close()
    }

    @QueryHandler
    fun getOrders(query: GetOrdersQuery): GetOrdersQueryResponse {
        val em = forName("SCSC")!!.newEntityManager
        val orderEntities = em
            .createQuery("SELECT p FROM OrderEntity AS p WHERE owner = ?1", OrderEntity::class.java)
            .setParameter(1, query.owner)
            .resultList
        val response = GetOrdersQueryResponse(
            orderEntities.stream()
                .map { orderEntity: OrderEntity ->
                    orderEntity.items.stream().map(OrderEntityItem::price)
                        .reduce(BigDecimal.ZERO) { obj: BigDecimal?, augend: BigDecimal? -> obj?.add(augend) }?.let {
                            GetOrdersQueryResponse.Order(
                                orderEntity.id!!,
                                it,
                                orderEntity.items.stream()
                                    .map { (_, name, price): OrderEntityItem ->
                                        OrderLine(
                                            name!!,
                                            price!!
                                        )
                                    }.collect(Collectors.toList()),
                                orderEntity.owner!!,
                                orderEntity.isPaid,
                                orderEntity.isPrepared,
                                orderEntity.isReady
                            )
                        }
                }
                .collect(Collectors.toList())
        )
        em.close()
        return response
    }

    @ResetHandler
    fun onReset(resetContext: ResetContext<*>?) {
        val em = forName("SCSC")!!.newEntityManager
        em.transaction.begin()
        em.createQuery("DELETE FROM OrderEntity").executeUpdate()
        em.transaction.commit()
    }

    private fun toEntity(orderCreatedEvent: OrderCreatedEvent): OrderEntity {
        val orderEntity = OrderEntity()
        orderEntity.id = orderCreatedEvent.orderId
        orderEntity.owner = orderCreatedEvent.owner
        val items: List<OrderEntityItem> = orderCreatedEvent.items.stream().map { item ->
            val entity = OrderEntityItem()
            entity.id = item.id
            entity.name = item.name
            entity.price = item.price
            entity
        }.collect(Collectors.toList())
        orderEntity.items = items
        return orderEntity
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
        private val LOG = LoggerFactory.getLogger(OrdersProjection::class.java)
    }
}
