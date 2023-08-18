package demo.scsc.api.payment

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.math.BigDecimal
import java.util.*

data class ProcessPaymentCommand(
    @TargetAggregateIdentifier val orderPaymentId: UUID,
    val amount: BigDecimal
)
