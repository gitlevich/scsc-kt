package demo.scsc.api.order

import java.math.BigDecimal
import java.util.*

class GetOrdersQueryResponse(val orders: List<Order>) {
    data class Order(
        val id: UUID,
        val total: BigDecimal,
        val lines: List<OrderLine>,
        val owner: String,
        val isPaid: Boolean,
        val isPrepared: Boolean,
        val isShipped: Boolean
    )

    data class OrderLine(val name: String, val price: BigDecimal)

}
