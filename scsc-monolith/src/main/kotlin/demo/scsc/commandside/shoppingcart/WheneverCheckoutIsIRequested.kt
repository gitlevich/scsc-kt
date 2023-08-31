package demo.scsc.commandside.shoppingcart

import demo.scsc.api.order
import demo.scsc.api.shoppingcart
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.DisallowReplay
import org.axonframework.eventhandling.EventHandler
import org.slf4j.LoggerFactory
import java.util.*

class WheneverCheckoutIsIRequested {

    @DisallowReplay
    @EventHandler
    fun on(event: shoppingcart.CartCheckoutRequestedEvent, commandGateway: CommandGateway, nextUuid: () -> UUID = { UUID.randomUUID() }) {
        log.debug("[   POLICY ] Whenever car checkout is requested, create an order (cartId=t {})", event.cartId)
        try {
            val orderId = commandGateway.send<UUID>(order.CreateOrderCommand(nextUuid(), event.owner, event.products)).join()
            log.debug("[   POLICY ] Checkout is complete for cart {}", event.cartId)
            commandGateway.send<Unit>(shoppingcart.CompleteCartCheckoutCommand(event.cartId, orderId))
        } catch (e: Exception) {
            commandGateway.send<Unit>(shoppingcart.HandleCartCheckoutFailureCommand(event.cartId))
            log.error("[   POLICY ] Order creation failed for cart A${event.cartId}. Cart notified. Error was: ${e.message}")
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(WheneverCheckoutIsIRequested::class.java)
    }
}
