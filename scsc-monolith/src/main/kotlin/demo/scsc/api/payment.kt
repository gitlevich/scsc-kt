package demo.scsc.api

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.math.BigDecimal
import java.util.*

@Suppress("ClassName")
object payment {

    data class ProcessPaymentCommand(@TargetAggregateIdentifier val orderPaymentId: UUID, val amount: BigDecimal)
    data class PaymentReceivedEvent(val orderPaymentId: UUID, val amount: BigDecimal)

    data class RequestPaymentCommand(val orderPaymentId: UUID, val orderId: UUID, val amount: BigDecimal)
    data class PaymentRequestedEvent(val orderPaymentId: UUID, val orderId: UUID, val amount: BigDecimal)
    data class OrderFullyPaidEvent(val orderPaymentId: UUID, val orderId: UUID)

    data class GetPaymentForOrderQuery(val orderId: UUID) {
        data class GetPaymentForOrderQueryResponse(
            val id: UUID,
            val orderId: UUID,
            val requestedAmount: BigDecimal,
            val paidAmount: BigDecimal
        )
    }
}
