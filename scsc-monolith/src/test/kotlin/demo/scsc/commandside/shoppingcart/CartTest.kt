package demo.scsc.commandside.shoppingcart

import demo.scsc.ShoppingCart.abandonCartCommand
import demo.scsc.ShoppingCart.addProductToCartCommand
import demo.scsc.ShoppingCart.cartAbandonedEvent
import demo.scsc.ShoppingCart.cartCheckoutCompletedEvent
import demo.scsc.ShoppingCart.cartCheckoutRequestedEvent
import demo.scsc.ShoppingCart.cartCreatedEvent
import demo.scsc.ShoppingCart.cartId
import demo.scsc.ShoppingCart.checkOutCartCommand
import demo.scsc.ShoppingCart.checkoutFailedEvent
import demo.scsc.ShoppingCart.completeCartCheckoutCommand
import demo.scsc.ShoppingCart.handleCheckoutFailureCommand
import demo.scsc.ShoppingCart.productAddedToCartEvent
import demo.scsc.ShoppingCart.productRemovedFromCartEvent
import demo.scsc.ShoppingCart.removeProductFromCartCommand
import demo.scsc.api.shoppingcart
import demo.scsc.commandside.shoppingcart.Cart.Companion.ABANDON_CART
import demo.scsc.commandside.shoppingcart.Cart.Companion.abandonCartAfter
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.test.aggregate.AggregateTestFixture
import org.junit.jupiter.api.Test

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
}
