package demo.scsc.queryside.order

import com.typesafe.config.Config
import demo.scsc.Constants
import demo.scsc.api.order.GetOrdersQuery
import demo.scsc.api.order.GetOrdersQueryResponse
import demo.scsc.api.order.GetOrdersQueryResponse.OrderLine
import demo.scsc.api.order.OrderCompletedEvent
import demo.scsc.api.order.OrderCreatedEvent
import demo.scsc.api.payment.OrderFullyPaidEvent
import demo.scsc.api.warehouse.PackageReadyEvent
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

@ProcessingGroup(Constants.PROCESSING_GROUP_ORDER)
class OrdersProjection(private val appConfig: Config) {
    @EventHandler
    fun on(orderCreatedEvent: OrderCreatedEvent) {
        tx(appConfig) { it.persist(orderCreatedEvent.toEntity()) }
    }

    @EventHandler
    fun on(orderFullyPaidEvent: OrderFullyPaidEvent) {
        tx(appConfig) {
            val orderEntity = it.find(Order::class.java, orderFullyPaidEvent.orderId)
            it.merge(orderEntity.copy(isPaid = true))
        }
    }

    @EventHandler
    fun on(packageReadyEvent: PackageReadyEvent) {
        tx(appConfig) {
            val orderEntity = it.find(Order::class.java, packageReadyEvent.orderId)
            it.merge(orderEntity.copy(isPrepared = true))
        }
    }

    @EventHandler
    fun on(orderCompletedEvent: OrderCompletedEvent) {
        tx(appConfig) {
            val orderEntity = it.find(Order::class.java, orderCompletedEvent.orderId)
            it.merge(orderEntity.copy(isReady = true))
        }
    }

    @QueryHandler
    fun getOrders(query: GetOrdersQuery): GetOrdersQueryResponse = tx(appConfig) { entityManager ->
        GetOrdersQueryResponse(
            entityManager
                .createQuery("SELECT p FROM Order AS p WHERE owner = ?1", Order::class.java)
                .setParameter(1, query.owner)
                .resultList
                .map { order: Order ->
                    order.items
                        .map { it.price }
                        .fold(BigDecimal.ZERO) { r, c -> r.add(c) }
                        .let { price ->
                            GetOrdersQueryResponse.Order(
                                id = order.id,
                                total = price,
                                lines = order.items
                                    .map { (_, name, price) -> OrderLine(name, price) }
                                    .toList(),
                                owner = order.owner,
                                isPaid = order.isPaid,
                                isPrepared = order.isPrepared,
                                isShipped = order.isReady
                            )
                        }
                }
                .toList()
        )
    }

    @ResetHandler
    fun onReset(resetContext: ResetContext<*>?) {
        tx(appConfig) { it.createQuery("DELETE FROM Order").executeUpdate() }
    }

    private fun OrderCreatedEvent.toEntity() = Order(
        id = orderId,
        owner = owner,
        items = items.map { item ->
            OrderItem(item.id, item.name, price = item.price)
        }
    )

    @MessageHandlerInterceptor(messageType = EventMessage::class)
    fun intercept(message: EventMessage<*>, interceptorChain: InterceptorChain) {
        LOG.info("[    EVENT ] " + message.payload.toString())
        interceptorChain.proceed()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(OrdersProjection::class.java)
    }
}
