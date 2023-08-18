package demo.scsc.api.payment

import java.math.BigDecimal
import java.util.*

data class PaymentReceivedEvent(val orderPaymentId: UUID, val amount: BigDecimal)
