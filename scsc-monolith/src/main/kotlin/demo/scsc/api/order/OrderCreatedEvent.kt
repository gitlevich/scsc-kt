package demo.scsc.api.order

import java.math.BigDecimal
import java.util.*

data class OrderCreatedEvent(val orderId: UUID, val owner: String, val items: List<OrderItem>) {
    data class OrderItem(val id: UUID, val name: String, val price: BigDecimal)
}
