package demo.scsc.queryside.order

import com.typesafe.config.Config
import demo.scsc.Constants
import demo.scsc.api.order.GetOrdersQuery
import demo.scsc.api.order.GetOrdersQuery.GetOrdersQueryResponse
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
import java.util.*

@ProcessingGroup(Constants.PROCESSING_GROUP_ORDER)
class OrdersProjection(private val appConfig: Config) {

    @QueryHandler
    fun getOrders(query: GetOrdersQuery): GetOrdersQueryResponse = tx(appConfig) { entityManager ->
        val orders = entityManager
            .createQuery("SELECT p FROM Order AS p WHERE owner = ?1", Order::class.java)
            .setParameter(1, query.owner)
            .resultList

        GetOrdersQueryResponse(
            orders.map { order ->
                order.items
                    .map { it.price }
                    .fold(BigDecimal.ZERO) { r, c -> r.add(c) }
                    .let { price ->
                        GetOrdersQueryResponse.Order(
                            id = order.id,
                            total = price.setScale(2),
                            lines = order.items
                                .map { (_, name, price) ->
                                    GetOrdersQueryResponse.OrderLine(
                                        name,
                                        price.setScale(2)
                                    )
                                }
                                .toList(),
                            owner = order.owner,
                            isPaid = order.isPaid,
                            isPrepared = order.isPrepared,
                            isShipped = order.isReady
                        )
                    }
            }
        )
    }
    @EventHandler
    fun on(event: OrderCreatedEvent) {
        tx(appConfig) { it.persist(event.toEntity()) }
    }

    @EventHandler
    fun on(event: OrderFullyPaidEvent) {
        updated(event.orderId) { it.copy(isPaid = true) }
    }

    @EventHandler
    fun on(event: PackageReadyEvent) {
        updated(event.orderId) { it.copy(isPrepared = true) }
    }

    @EventHandler
    fun on(event: OrderCompletedEvent) {
        updated(event.orderId) { it.copy(isReady = true) }
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

    private fun updated(orderId: UUID, update: (Order) -> Order) {
        tx(appConfig) { em ->
            em.find(Order::class.java, orderId)
                ?.let { order -> em.merge(update(order)) }
                ?: LOG.warn("Order with id $orderId not found")
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(OrdersProjection::class.java)
    }
}
