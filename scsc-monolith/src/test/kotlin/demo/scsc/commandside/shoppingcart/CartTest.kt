package demo.scsc.commandside.shoppingcart

import demo.scsc.api.productCatalog
import demo.scsc.api.shoppingCart
import demo.scsc.commandside.shoppingcart.Cart.Companion.ABANDON_CART
import demo.scsc.commandside.shoppingcart.Cart.Companion.abandonCartAfter
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.test.aggregate.AggregateTestFixture
import org.junit.jupiter.api.Test
import java.math.BigDecimal
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
            .expectEvents(cartAbandonedEvent.copy(reason = shoppingCart.CartAbandonedEvent.Reason.MANUAL))
    }

    @Test
    fun `should attempt to create a new Order on CheckOutCartCommand`() {
        cart.given(productUpdateReceivedEvent, cartCreatedEvent, productAddedToCartEvent)
            .`when`(checkOutCartCommand)
            .expectEvents(cartCheckedOutEvent)
    }

    @Test
    fun `should publish CartCheckedOutEvent on CheckOutCartCommand`() {
        cart
            .given(
                cartCreatedEvent,
                productAddedToCartEvent,
            )
            .`when`(checkOutCartCommand)
            .expectEvents(cartCheckedOutEvent)
    }


    companion object {
        private val cartId = UUID.fromString("00000000-0000-0000-0000-0c09a0d0d88e")

        private val addProductToCartCommand = shoppingCart.AddProductToCartCommand(
            cartId = cartId,
            productId = UUID.randomUUID(),
            owner = "John Doe"
        )
        private val cartCreatedEvent = shoppingCart.CartCreatedEvent(
            id = cartId,
            owner = addProductToCartCommand.owner
        )
        private val productAddedToCartEvent = shoppingCart.ProductAddedToCartEvent(
            cartId = cartId,
            productId = addProductToCartCommand.productId
        )

        private val removeProductFromCartCommand = shoppingCart.RemoveProductFromCartCommand(
            cartId = cartId,
            productId = addProductToCartCommand.productId
        )
        private val productRemovedFromCartEvent = shoppingCart.ProductRemovedFromCartEvent(
            cartId = cartId,
            productId = addProductToCartCommand.productId
        )

        private val abandonCartCommand = shoppingCart.AbandonCartCommand(
            cartId = cartId
        )
        private val cartAbandonedEvent = shoppingCart.CartAbandonedEvent(
            cartId = cartId,
            reason = shoppingCart.CartAbandonedEvent.Reason.TIMEOUT
        )

        private val checkOutCartCommand = shoppingCart.CheckOutCartCommand(
            cartId = cartId
        )
        private val cartCheckedOutEvent = shoppingCart.CartCheckedOutEvent(
            cartId = cartId
        )

        private val productUpdateReceivedEvent = productCatalog.ProductUpdateReceivedEvent(
            id = addProductToCartCommand.productId,
            name = "name",
            price = BigDecimal.valueOf(1.0),
            image = "image",
            desc = "desc",
            onSale = true
        )
    }
}
