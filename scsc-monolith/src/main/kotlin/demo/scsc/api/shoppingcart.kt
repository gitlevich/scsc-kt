package demo.scsc.api

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.*

object shoppingcart {
    data class AddProductToCartCommand(
        @TargetAggregateIdentifier val cartId: UUID,
        val owner: String,
        val productId: UUID
    )
    data class CartCreatedEvent(val id: UUID, val owner: String)
    data class ProductAddedToCartEvent(val cartId: UUID, val productId: UUID)

    data class RemoveProductFromCartCommand(@TargetAggregateIdentifier val cartId: UUID, val productId: UUID)
    data class ProductRemovedFromCartEvent(val cartId: UUID, val productId: UUID)

    data class CheckOutCartCommand(@TargetAggregateIdentifier val cartId: UUID)
    data class CartCheckoutRequestedEvent(val cartId: UUID, val owner: String, val products: List<UUID>)

    data class CompleteCartCheckoutCommand(@TargetAggregateIdentifier val cartId: UUID, val orderId: UUID)
    data class CartCheckoutCompletedEvent(val cartId: UUID)

    data class HandleCartCheckoutFailureCommand(@TargetAggregateIdentifier val cartId: UUID)
    data class CartCheckoutFailedEvent(val cartId: UUID)

    data class GetCartQuery(val owner: String) {
        data class Response(val cartId: UUID, val products: List<UUID>) {
            operator fun plus(productId: UUID) = copy(products = products + productId)
            operator fun minus(productId: UUID) = copy(products = products - productId)
        }
    }

    data class AbandonCartCommand(@TargetAggregateIdentifier val cartId: UUID)
    data class CartAbandonedEvent(val cartId: UUID, val reason: Reason) {
        enum class Reason { MANUAL, TIMEOUT }
    }
}
