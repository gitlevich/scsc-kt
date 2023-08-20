package demo.scsc.api

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.*

object shoppingCart {
    data class AddProductToCartCommand(
        @TargetAggregateIdentifier val cartId: UUID,
        val owner: String,
        val productId: UUID
    )
    data class ProductAddedToCartEvent(val cartId: UUID, val productId: UUID)
    data class RemoveProductFromCartCommand(@TargetAggregateIdentifier val cartId: UUID, val productId: UUID)
    data class ProductRemovedFromCartEvent(val cartId: UUID, val productId: UUID)

    data class CheckOutCartCommand(@TargetAggregateIdentifier val cartId: UUID)
    data class CartCheckedOutEvent(val cartId: UUID)

    data class CartCreatedEvent(val id: UUID, val owner: String)

    data class GetCartQuery(val owner: String) {
        data class Response(val cartId: UUID, val products: List<UUID>)
    }

    data class AbandonCartCommand(@TargetAggregateIdentifier val cartId: UUID)
    data class CartAbandonedEvent(val cartId: UUID, val reason: Reason) {
        enum class Reason { MANUAL, TIMEOUT }
    }
}
