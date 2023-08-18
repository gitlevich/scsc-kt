package demo.scsc.api.shoppingcart

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.*

data class AbandonCartCommand(@TargetAggregateIdentifier val cartId: UUID)
