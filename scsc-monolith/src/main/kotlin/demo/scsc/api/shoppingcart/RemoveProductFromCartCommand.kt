package demo.scsc.api.shoppingcart

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.*

data class RemoveProductFromCartCommand(
    @TargetAggregateIdentifier val cartId: UUID,
    val productId: UUID
)
