package demo.scsc.commandside.shoppingcart

import demo.scsc.api.shoppingCart
import demo.scsc.commandside.order.Order
import demo.scsc.util.attemptTo
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
    private lateinit var owner: String
    private val products: MutableList<UUID> = mutableListOf()

    @CommandHandler
    @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
    fun handle(command: shoppingCart.AddProductToCartCommand, deadlineManager: DeadlineManager): UUID {

        applyEvent(shoppingCart.CartCreatedEvent(UUID.randomUUID(), command.owner))
        if (products.contains(command.productId))
            throw CommandExecutionException("Product already in the cart! ", null)

        applyEvent(shoppingCart.ProductAddedToCartEvent(id, command.productId))
        deadlineManager.schedule(Duration.ofMinutes(10), ABANDON_CART)
        return id
    }

    @CommandHandler
    fun handle(command: shoppingCart.RemoveProductFromCartCommand) {
        if (!products.contains(command.productId)) throw CommandExecutionException("Product not in the cart! ", null)

        applyEvent(shoppingCart.ProductRemovedFromCartEvent(id, command.productId))
    }

    @CommandHandler
    fun handle(command: shoppingCart.AbandonCartCommand) {
        applyEvent(shoppingCart.CartAbandonedEvent(command.cartId, shoppingCart.CartAbandonedEvent.Reason.MANUAL))
    }

    @CommandHandler
    fun handle(command: shoppingCart.CheckOutCartCommand) {
        attemptTo { createNew(Order::class.java) { Order(products, owner) } }
        applyEvent(shoppingCart.CartCheckedOutEvent(command.cartId))
    }

    @DeadlineHandler(deadlineName = ABANDON_CART)
    fun onDeadline() {
        applyEvent(shoppingCart.CartAbandonedEvent(id, shoppingCart.CartAbandonedEvent.Reason.TIMEOUT))
    }

    @EventSourcingHandler
    fun on(event: shoppingCart.CartCreatedEvent) {
        id = event.id
        owner = event.owner
    }

    @EventSourcingHandler
    fun on(event: shoppingCart.ProductAddedToCartEvent) {
        products.add(event.productId)
    }

    @EventSourcingHandler
    fun on(event: shoppingCart.ProductRemovedFromCartEvent) {
        products.remove(event.productId)
    }

    @EventSourcingHandler
    fun on(event: shoppingCart.CartAbandonedEvent) {
        markDeleted()
    }

    @EventSourcingHandler
    fun on(event: shoppingCart.CartCheckedOutEvent) {
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
        private const val ABANDON_CART = "abandon-cart"
        private val LOG = LoggerFactory.getLogger(Cart::class.java)
    }
}
