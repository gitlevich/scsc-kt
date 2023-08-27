package demo.scsc.commandside.shoppingcart

import demo.scsc.api.shoppingcart
import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.deadline.DeadlineManager
import org.axonframework.deadline.annotation.DeadlineHandler
import org.axonframework.eventhandling.EventMessage
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.extensions.kotlin.applyEvent
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.interceptors.MessageHandlerInterceptor
import org.axonframework.modelling.command.AggregateCreationPolicy
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.*
import org.axonframework.modelling.command.AggregateRoot
import org.axonframework.modelling.command.CreationPolicy
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*

@AggregateRoot
class Cart() {
    @AggregateIdentifier
    lateinit var id: UUID
    internal lateinit var owner: String
    internal val products: MutableList<UUID> = mutableListOf()

    @CommandHandler
    @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
    fun handle(
        command: shoppingcart.AddProductToCartCommand,
        deadlineManager: DeadlineManager,
        nextUuid: () -> UUID = { UUID.randomUUID() }
    ): UUID {
        val cartId = nextUuid()
        applyEvent(shoppingcart.CartCreatedEvent(cartId, command.owner))
        if (products.contains(command.productId))
            throw CommandExecutionException("Product already in the cart! ", null)

        applyEvent(shoppingcart.ProductAddedToCartEvent(cartId, command.productId))
        deadlineManager.schedule(abandonCartAfter, ABANDON_CART)
        return cartId
    }

    @CommandHandler
    fun handle(command: shoppingcart.RemoveProductFromCartCommand) {
        if (!products.contains(command.productId)) throw CommandExecutionException("Product not in the cart! ", null)

        applyEvent(shoppingcart.ProductRemovedFromCartEvent(id, command.productId))
    }

    @CommandHandler
    fun handle(command: shoppingcart.AbandonCartCommand) {
        applyEvent(shoppingcart.CartAbandonedEvent(command.cartId, shoppingcart.CartAbandonedEvent.Reason.MANUAL))
    }

    @CommandHandler
    fun handle(command: shoppingcart.CheckOutCartCommand) {
        applyEvent(shoppingcart.CartCheckoutRequestedEvent(command.cartId, owner, products))
    }

    @CommandHandler
    fun handle(command: shoppingcart.CompleteCartCheckoutCommand) {
        applyEvent(shoppingcart.CartCheckoutCompletedEvent(command.cartId))
    }

    @CommandHandler
    fun handle(command: shoppingcart.HandleCartCheckoutFailureCommand) {
        applyEvent(shoppingcart.CartCheckoutFailedEvent(command.cartId))
    }

    @DeadlineHandler(deadlineName = ABANDON_CART)
    fun onDeadline() {
        applyEvent(shoppingcart.CartAbandonedEvent(id, shoppingcart.CartAbandonedEvent.Reason.TIMEOUT))
    }

    @EventSourcingHandler
    fun on(event: shoppingcart.CartCreatedEvent) {
        id = event.id
        owner = event.owner
    }

    @EventSourcingHandler
    fun on(event: shoppingcart.ProductAddedToCartEvent) {
        products.add(event.productId)
    }

    @EventSourcingHandler
    fun on(event: shoppingcart.ProductRemovedFromCartEvent) {
        products.remove(event.productId)
    }

    @EventSourcingHandler
    fun on(event: shoppingcart.CartAbandonedEvent) {
        markDeleted()
    }

    @EventSourcingHandler
    fun on(event: shoppingcart.CartCheckoutCompletedEvent) {
        markDeleted()
    }

    @MessageHandlerInterceptor(messageType = CommandMessage::class)
    fun intercept(message: CommandMessage<*>, interceptorChain: InterceptorChain) {
        LOG.info("[  COMMAND ] ${message.payload}")
        interceptorChain.proceed()
    }

    @MessageHandlerInterceptor(messageType = EventMessage::class)
    fun intercept(message: EventMessage<*>, interceptorChain: InterceptorChain) {
        if (isLive()) LOG.info("[    EVENT ] ${message.payload}") else LOG.info("[ SOURCING ] ${message.payload}")
        interceptorChain.proceed()
    }

    companion object {
        internal const val ABANDON_CART: String = "abandon-cart"
        internal val abandonCartAfter = Duration.ofMinutes(10)
        private val LOG = LoggerFactory.getLogger(Cart::class.java)
    }
}
