package demo.scsc.queryside.shoppingcart

import demo.scsc.ShoppingCart.cartAbandonedEvent
import demo.scsc.ShoppingCart.cartCheckoutCompletedEvent
import demo.scsc.ShoppingCart.cartCreatedEvent
import demo.scsc.ShoppingCart.productAddedToCartEvent
import demo.scsc.ShoppingCart.productRemovedFromCartEvent
import demo.scsc.TestScenario
import demo.scsc.api.shoppingcart
import demo.scsc.appliedTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.stream.Stream

class CartsProjectionTest {

    private val projection = CartsProjection()

    @ParameterizedTest
    @ArgumentsSource(ScenarioProvider::class)
    fun theTest(scenario: TestScenario<*, *>) {
        scenario.appliedTo(projection)
        assertThat(projection.handle(getCartQuery)).isEqualTo(scenario.result)
    }

    @Before
    fun setUp() {
        projection.on(cartAbandonedEvent)
    }

    companion object {
        private val getCartQuery = shoppingcart.GetCartQuery(owner = cartCreatedEvent.owner)
        private class ScenarioProvider : ArgumentsProvider {
            override fun provideArguments(context: ExtensionContext): Stream<Arguments> =
                listOf(
                    TestScenario(
                        "should persist the cart on CartCreatedEvent",
                        listOf(cartCreatedEvent),
                        shoppingcart.GetCartQuery.Response(cartCreatedEvent.id, emptyList())
                    ),
                    TestScenario(
                        "should add a product to cart on ProductAddedToCartEvent",
                        listOf(cartCreatedEvent, productAddedToCartEvent),
                        shoppingcart.GetCartQuery.Response(
                            cartCreatedEvent.id,
                            listOf(productAddedToCartEvent.productId)
                        )
                    ),
                    TestScenario(
                        "should remove a product from cart on ProductRemovedFromCartEvent",
                        listOf(cartCreatedEvent, productAddedToCartEvent, productRemovedFromCartEvent),
                        shoppingcart.GetCartQuery.Response(
                            cartCreatedEvent.id,
                            emptyList()
                        )
                    ),
                    TestScenario(
                        "should remove the cart on CartCheckoutCompletedEvent",
                        listOf(cartCreatedEvent, productAddedToCartEvent, cartCheckoutCompletedEvent),
                        null
                    ),
                    TestScenario(
                        "should remove the cart on CartAbandonedEvent",
                        listOf(cartCreatedEvent, cartAbandonedEvent),
                        null
                    ),
                ).stream().map { Arguments.of(it) }
        }
    }
}

