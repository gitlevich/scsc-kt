package demo.scsc.api.payment

import java.math.BigDecimal
import java.util.*

data class RequestPaymentCommand(val orderPaymentId: UUID, val orderId: UUID, val amount: BigDecimal)
