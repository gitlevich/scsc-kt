package demo.scsc.commandside.shoppingcart

import demo.scsc.api.shoppingcart
import demo.scsc.commandside.shoppingcart.Cart.Companion.ABANDON_CART
import demo.scsc.commandside.shoppingcart.Cart.Companion.abandonCartAfter
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.test.aggregate.AggregateTestFixture
import org.junit.jupiter.api.Test
import java.util.*

class CartTest {
    private val cart = AggregateTestFixture(Cart::class.java).also {
        it.registerInjectableResource { cartId }
    }

    @Test
    fun `should publish CartCreatedEvent and ProductAddedToCartEvent on AddProductToCartCommand`() {
        cart.givenNoPriorActivity()
            .`when`(addProductToCartCommand)
            .expectEvents(cartCreatedEvent, productAddedToCartEvent)
    }

    @Test
    fun `should set cart state on AddProductToCartCommand`() {
        cart.givenNoPriorActivity()
            .`when`(addProductToCartCommand)
            .expectState { cart ->
                assertThat(cart.id).isEqualTo(cartCreatedEvent.id)
                assertThat(cart.owner).isEqualTo(cartCreatedEvent.owner)
                assertThat(cart.products).contains(productAddedToCartEvent.productId)
            }
    }

    @Test
    fun `should complain if AddProductToCartCommand attempts to add the product contained in cart`() {
        // TODO why? Can't I add several of them? Changed the set to list for now, then will start keeping track of quantity
        cart.given(cartCreatedEvent, productAddedToCartEvent)
            .`when`(addProductToCartCommand)
            .expectException(CommandExecutionException::class.java)
    }

    @Test
    fun `should schedule ABANDON_CART deadline on AddProductToCartCommand`() {
        cart.givenNoPriorActivity()
            .`when`(addProductToCartCommand)
            .expectScheduledDeadlineWithName(abandonCartAfter, ABANDON_CART)
    }

    @Test
    fun `should publish CartAbandonedEvent on ABANDON_CART deadline`() {
        cart.givenCommands(addProductToCartCommand)
            .whenTimeElapses(abandonCartAfter)
            .expectTriggeredDeadlinesWithName(ABANDON_CART)
            .expectEvents(cartAbandonedEvent)
    }

    @Test
    fun `should complain on AddProductToCartCommand if the cart is already created - per creation policy`() {
        cart.givenCommands(addProductToCartCommand)
            .`when`(addProductToCartCommand)
            .expectException(CommandExecutionException::class.java)
    }

    @Test
    fun `should publish ProductRemovedFromCartEvent on RemoveProductFromCartCommand when the product is in the cart`() {
        cart.given(cartCreatedEvent, productAddedToCartEvent)
            .`when`(removeProductFromCartCommand)
            .expectEvents(productRemovedFromCartEvent)
    }

    @Test
    fun `should complain on RemoveProductFromCartCommand when the product is NOT in the cart`() {
        cart.given(cartCreatedEvent)
            .`when`(removeProductFromCartCommand)
            .expectException(CommandExecutionException::class.java)
    }

    @Test
    fun `should remove the product from the cart on ProductRemovedFromCartEvent`() {
        cart.given(cartCreatedEvent, productAddedToCartEvent)
            .`when`(removeProductFromCartCommand)
            .expectState { cart ->
                assertThat(cart.products).doesNotContain(productAddedToCartEvent.productId)
            }
    }

    @Test
    fun `on AbandonCartCommand, should publish CartAbandonedEvent`() {
        cart.given(cartCreatedEvent, productAddedToCartEvent)
            .`when`(abandonCartCommand)
            .expectEvents(cartAbandonedEvent.copy(reason = shoppingcart.CartAbandonedEvent.Reason.MANUAL))
    }

    @Test
    fun `should publish CartCheckoutRequestedEvent on CheckOutCartCommand`() {
        cart.given(cartCreatedEvent, productAddedToCartEvent)
            .`when`(checkOutCartCommand)
            .expectEvents(cartCheckoutRequestedEvent)
    }

    @Test
    fun `should publish CartCheckoutCompletedEvent on CompleteCartCheckoutCommand`() {
        cart
            .given(
                cartCreatedEvent,
                productAddedToCartEvent,
                cartCheckoutRequestedEvent
            )
            .`when`(completeCartCheckoutCommand)
            .expectEvents(cartCheckoutCompletedEvent)
    }

    @Test
    fun `should publish CartCheckoutFailedEvent on HandleCartCheckoutFailureCommand`() {
        cart
            .given(
                cartCreatedEvent,
                productAddedToCartEvent,
                cartCheckoutRequestedEvent
            )
            .`when`(handleCheckoutFailureCommand)
            .expectEvents(checkoutFailedEvent)
    }


    companion object {
        private val cartId = UUID.fromString("00000000-0000-0000-0000-0c09a0d0d88e")

        internal val addProductToCartCommand = shoppingcart.AddProductToCartCommand(
            cartId = cartId,
            productId = UUID.randomUUID(),
            owner = "John Doe"
        )
        private val cartCreatedEvent = shoppingcart.CartCreatedEvent(
            id = cartId,
            owner = addProductToCartCommand.owner
        )
        private val productAddedToCartEvent = shoppingcart.ProductAddedToCartEvent(
            cartId = cartId,
            productId = addProductToCartCommand.productId
        )

        private val removeProductFromCartCommand = shoppingcart.RemoveProductFromCartCommand(
            cartId = cartId,
            productId = addProductToCartCommand.productId
        )
        private val productRemovedFromCartEvent = shoppingcart.ProductRemovedFromCartEvent(
            cartId = cartId,
            productId = addProductToCartCommand.productId
        )

        private val abandonCartCommand = shoppingcart.AbandonCartCommand(
            cartId = cartId
        )
        private val cartAbandonedEvent = shoppingcart.CartAbandonedEvent(
            cartId = cartId,
            reason = shoppingcart.CartAbandonedEvent.Reason.TIMEOUT
        )

        private val checkOutCartCommand = shoppingcart.CheckOutCartCommand(cartId = cartId)
        internal val cartCheckoutRequestedEvent = shoppingcart.CartCheckoutRequestedEvent(
            cartId = cartId,
            owner = addProductToCartCommand.owner,
            products = listOf(addProductToCartCommand.productId)
        )

        internal val completeCartCheckoutCommand = shoppingcart.CompleteCartCheckoutCommand(
            cartId = cartId,
            orderId = UUID.randomUUID()
        )
        private val cartCheckoutCompletedEvent = shoppingcart.CartCheckoutCompletedEvent(cartId = cartId)

        internal val handleCheckoutFailureCommand = shoppingcart.HandleCartCheckoutFailureCommand(cartId = cartId)
        private val checkoutFailedEvent = shoppingcart.CartCheckoutFailedEvent(cartId = cartId)
    }
}
