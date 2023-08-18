package demo.scsc.api.payment

import java.util.*

data class OrderFullyPaidEvent(val orderPaymentId: UUID, val orderId: UUID)
