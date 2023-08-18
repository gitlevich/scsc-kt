package demo.scsc.api.payment

import java.math.BigDecimal
import java.util.*

data class GetPaymentForOrderQueryResponse(
    val id: UUID,
    val orderId: UUID,
    val requestedAmount: BigDecimal,
    val paidAmount: BigDecimal
)
