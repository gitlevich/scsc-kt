package demo.scsc.api.shoppingcart

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.*

data class AddProductToCartCommand(
    @TargetAggregateIdentifier val cartId: UUID,
    val owner: String,
    val productId: UUID
)
