package demo.scsc.queryside.shoppingcart

import demo.scsc.api.shoppingcart
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.*
import java.util.stream.Stream

class CartsProjectionTest {

    private val projection = CartsProjection()

    @ParameterizedTest
    @ArgumentsSource(ScenarioProvider::class)
    fun theTest(scenario: Scenario<*>) {
        scenario.appliedTo(projection)
        assertThat(projection.handle(getCartQuery)).isEqualTo(scenario.result)
    }

    @Before
    fun setUp() {
        projection.on(cartAbandonedEvent)
    }

    companion object {
        private val cartCreatedEvent = shoppingcart.CartCreatedEvent(
            id = UUID.randomUUID(),
            owner = "test"
        )
        private val productAddedToCartEvent = shoppingcart.ProductAddedToCartEvent(
            cartId = cartCreatedEvent.id,
            productId = UUID.randomUUID()
        )

        private val productRemovedFromCartEvent = shoppingcart.ProductRemovedFromCartEvent(
            cartId = cartCreatedEvent.id,
            productId = productAddedToCartEvent.productId
        )

        private val cartAbandonedEvent = shoppingcart.CartAbandonedEvent(
            cartId = cartCreatedEvent.id,
            reason = shoppingcart.CartAbandonedEvent.Reason.MANUAL
        )

        private val cartCheckoutCompletedEvent = shoppingcart.CartCheckoutCompletedEvent(cartId = cartCreatedEvent.id)

        private val getCartQuery = shoppingcart.GetCartQuery(owner = cartCreatedEvent.owner)

        private class ScenarioProvider : ArgumentsProvider {
            override fun provideArguments(context: ExtensionContext): Stream<Arguments> =
                listOf(
                    Scenario(
                        "should persist the cart on CartCreatedEvent",
                        listOf(cartCreatedEvent),
                        shoppingcart.GetCartQuery.Response(cartCreatedEvent.id, emptyList())
                    ),
                    Scenario(
                        "should add a product to cart on ProductAddedToCartEvent",
                        listOf(cartCreatedEvent, productAddedToCartEvent),
                        shoppingcart.GetCartQuery.Response(
                            cartCreatedEvent.id,
                            listOf(productAddedToCartEvent.productId)
                        )
                    ),
                    Scenario(
                        "should remove a product from cart on ProductRemovedFromCartEvent",
                        listOf(cartCreatedEvent, productAddedToCartEvent, productRemovedFromCartEvent),
                        shoppingcart.GetCartQuery.Response(
                            cartCreatedEvent.id,
                            emptyList()
                        )
                    ),
                    Scenario(
                        "should remove the cart on CartCheckoutCompletedEvent",
                        listOf(cartCreatedEvent, productAddedToCartEvent, cartCheckoutCompletedEvent),
                        null
                    ),
                    Scenario(
                        "should remove the cart on CartAbandonedEvent",
                        listOf(cartCreatedEvent, cartAbandonedEvent),
                        null
                    ),
                ).stream().map { Arguments.of(it) }
        }
    }
}

data class Scenario<T>(val name: String, val events: List<T>, val result: shoppingcart.GetCartQuery.Response?) {
    fun appliedTo(projection: CartsProjection) = events.forEach { event ->
        projection::class.java.methods
            .find { it.name == "on" && it.parameterTypes.firstOrNull() == event!!::class.java }
            ?.invoke(projection, event)
    }
}
