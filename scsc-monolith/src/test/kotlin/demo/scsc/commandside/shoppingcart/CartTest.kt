package demo.scsc.commandside.shoppingcart

import demo.scsc.api.shoppingCart
import org.junit.Test
import java.util.*

class CartTest {

    @Test
    fun `should publish CartCreatedEvent and ProductAddedToCartEvent on AddProductToCartCommand`() {
        TODO("Implement me!")
    }

    @Test
    fun `should set cart id and owner on CartCreatedEvent`() {
        TODO("Implement me!")
    }

    @Test
    fun `should add the product to the cart on AddProductToCartCommand`() {
        TODO("Implement me!")
    }

    @Test
    fun `should complain if AddProductToCartCommand attempts to add the product contained in cart`() {
        // TODO why? Can't I add several of them? Changed the set to list for now, then will start keeping track of quantity
        TODO("Implement me!")
    }

    @Test
    fun `should schedule ABANDON_CART deadline on AddProductToCartCommand`() {
        TODO("Implement me!")
    }

    @Test
    fun `should publish CartAbandonedEvent on ABANDON_CART deadline`() {
        TODO("Implement me!")
    }

    @Test
    fun `should complain on AddProductToCartCommand if the cart is already created - per creation policy`() {
        TODO("Implement me!")
    }

    @Test
    fun `should publish ProductRemovedFromCartEvent on RemoveProductFromCartCommand when the product is in the cart`() {
        TODO("Implement me!")
    }

    @Test
    fun `should complain on RemoveProductFromCartCommand when the product is NOT in the cart`() {
        TODO("Implement me!")
    }

    @Test
    fun `should remove the product from the cart on ProductRemovedFromCartEvent`() {
        TODO("Implement me!")
    }

    @Test
    fun `should mark the cart deleted on CartAbandonedEvent`() {
        TODO("Implement me!")
    }

    @Test
    fun `should attempt to create a new Order on CheckOutCartCommand`() {
        TODO("Implement me!")
    }

    @Test
    fun `should publish CartCheckedOutEvent on CheckOutCartCommand`() {
        TODO("Implement me!")
    }


    companion object {
        private val cartId = UUID.randomUUID()
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
    }
}
