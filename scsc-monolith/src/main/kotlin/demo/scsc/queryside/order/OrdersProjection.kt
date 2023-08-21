package demo.scsc.queryside.order

import demo.scsc.Constants
import demo.scsc.api.order.GetOrdersQuery
import demo.scsc.api.order.GetOrdersQueryResponse
import demo.scsc.api.order.GetOrdersQueryResponse.OrderLine
import demo.scsc.api.order.OrderCompletedEvent
import demo.scsc.api.order.OrderCreatedEvent
import demo.scsc.api.payment.OrderFullyPaidEvent
import demo.scsc.api.warehouse.PackageReadyEvent
import demo.scsc.config.JpaPersistenceUnit.Companion.forName
import demo.scsc.util.tx
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
        tx { it.persist(toEntity(orderCreatedEvent)) }
    }

    @EventHandler
    fun on(orderFullyPaidEvent: OrderFullyPaidEvent) {
        tx {
            val orderEntity = it.find(OrderEntity::class.java, orderFullyPaidEvent.orderId)
            orderEntity.isPaid = true
            it.merge(orderEntity)
        }
    }

    @EventHandler
    fun on(packageReadyEvent: PackageReadyEvent) {
        tx {
            val orderEntity = it.find(OrderEntity::class.java, packageReadyEvent.orderId)
            orderEntity.isPrepared = true
            it.merge(orderEntity)
        }
    }

    @EventHandler
    fun on(orderCompletedEvent: OrderCompletedEvent) {
        tx {
            val orderEntity = it.find(OrderEntity::class.java, orderCompletedEvent.orderId)
            orderEntity.isReady = true
            it.merge(orderEntity)
        }
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
                        .reduce(BigDecimal.ZERO) { obj: BigDecimal?, augend: BigDecimal? -> obj?.add(augend) }
                        ?.let { price ->
                            GetOrdersQueryResponse.Order(
                                id = orderEntity.id!!,
                                total = price,
                                lines = orderEntity.items.stream()
                                    .map { (_, name, price): OrderEntityItem -> OrderLine(name!!, price!!) }
                                    .collect(Collectors.toList()),
                                owner = orderEntity.owner!!,
                                isPaid = orderEntity.isPaid,
                                isPrepared = orderEntity.isPrepared,
                                isShipped = orderEntity.isReady
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
        tx { it.createQuery("DELETE FROM OrderEntity").executeUpdate() }
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
    fun intercept(message: EventMessage<*>, interceptorChain: InterceptorChain) {
        LOG.info("[    EVENT ] " + message.payload.toString())
        interceptorChain.proceed()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(OrdersProjection::class.java)
    }
}
