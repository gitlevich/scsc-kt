package demo.scsc.commandside.shoppingcart

import demo.scsc.api.order
import demo.scsc.commandside.order.OrderTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.commandhandling.gateway.CommandGateway
import org.junit.jupiter.api.Test
import java.util.*
import java.util.concurrent.CompletableFuture

class WheneverCheckoutIsIRequestedTest {

    private val commandGateway = mockk<CommandGateway>(relaxed = true).also {
        every { it.send<UUID>(createOrderCommand) } returns CompletableFuture.completedFuture(orderId)
    }
    private val policy = WheneverCheckoutIsIRequested()

    @Test
    fun `should send CreateOrderCommand and CompleteCartCheckoutCommand on CartCheckoutRequestedEvent`() {
        policy.on(cartCheckoutRequestedEvent, commandGateway)

        verify { commandGateway.send<UUID>(createOrderCommand) }
        verify { commandGateway.send<Unit>(completeCartCheckoutCommand) }
    }

    @Test
    fun `should send HandleCartCheckoutFailureCommand on CartCheckoutRequestedEvent when order creation failed`() {
        every { commandGateway.send<UUID>(createOrderCommand) } returns CompletableFuture.failedFuture(Exception("error"))
        policy.on(cartCheckoutRequestedEvent, commandGateway)

        verify { commandGateway.send<UUID>(handleCartCheckoutFailureCommand) }
    }

    companion object {
        private val orderId = UUID.randomUUID()
        private val createOrderCommand = order.CreateOrderCommand(
            owner = CartTest.addProductToCartCommand.owner,
            itemIds = listOf(CartTest.addProductToCartCommand.productId)
        )
        private val cartCheckoutRequestedEvent = CartTest.cartCheckoutRequestedEvent
        private val completeCartCheckoutCommand = CartTest.completeCartCheckoutCommand.copy(orderId = orderId)
        private val handleCartCheckoutFailureCommand = CartTest.handleCheckoutFailureCommand
    }
}
