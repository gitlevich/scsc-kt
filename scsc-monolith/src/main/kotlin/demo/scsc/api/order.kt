package demo.scsc.api

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.math.BigDecimal
import java.util.*

object order {
    data class CreateOrderCommand(val owner: String, val itemIds: List<UUID>)
    data class CompleteOrderCommand(@TargetAggregateIdentifier val orderId: UUID)
    data class GetOrdersQuery(val owner: String, val orderId: String) {
        data class GetOrdersQueryResponse(val orders: List<Order>) {
            data class Order(
                val id: UUID,
                val total: BigDecimal, // TODO switch to monetary amount
                val lines: List<OrderLine>,
                val owner: String,
                val isPaid: Boolean,
                val isPrepared: Boolean,
                val isShipped: Boolean
            )

            data class OrderLine(val name: String, val price: BigDecimal)
        }
    }

    data class OrderCompletedEvent(val orderId: UUID)

    data class OrderCreatedEvent(val orderId: UUID, val owner: String, val items: List<OrderItem>) {
        data class OrderItem(val id: UUID, val name: String, val price: BigDecimal)
    }
}
