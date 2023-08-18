package demo.scsc.api.order

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.*

data class CompleteOrderCommand(@TargetAggregateIdentifier val orderId: UUID)
