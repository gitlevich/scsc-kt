package demo.scsc.commandside.shoppingcart

import demo.scsc.api.shoppingcart.*
import demo.scsc.commandside.order.Order
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
    var id: UUID? = null
    private var owner: String? = null
    private val products: MutableSet<UUID> = HashSet()

    @CommandHandler
    @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
    fun handle(command: AddProductToCartCommand, deadlineManager: DeadlineManager): UUID {

        if (id == null) applyEvent(CartCreatedEvent(UUID.randomUUID(), command.owner))
        if (products.contains(command.productId))
            throw CommandExecutionException("Product already in the cart! ", null)

                applyEvent(ProductAddedToCartEvent(id!!, command.productId))
        deadlineManager.schedule(Duration.ofMinutes(10), ABANDON_CART)
        return id!!
    }

    @CommandHandler
    fun handle(command: RemoveProductFromCartCommand) {

        if (!products.contains(command.productId)) throw CommandExecutionException("Product not in the cart! ", null)

        applyEvent(ProductRemovedFromCartEvent(id!!, command.productId))
    }

    @CommandHandler
    fun handle(command: AbandonCartCommand) {

        applyEvent(CartAbandonedEvent(command.cartId, CartAbandonedEvent.Reason.MANUAL))
    }

    @CommandHandler
    fun handle(command: CheckOutCartCommand) {

        try {
            createNew(Order::class.java) { Order(products.stream().toList(), owner!!) }
        } catch (e: Exception) {
            e.printStackTrace()
            throw CommandExecutionException(e.message, e)
        }
        applyEvent(CartCheckedOutEvent(command.cartId))
    }

    @DeadlineHandler(deadlineName = ABANDON_CART)
    fun onDeadline() {
        applyEvent(CartAbandonedEvent(id!!, CartAbandonedEvent.Reason.TIMEOUT))
    }

    @EventSourcingHandler
    fun on(cartCreatedEvent: CartCreatedEvent) {
        id = cartCreatedEvent.id
        owner = cartCreatedEvent.owner
    }

    @EventSourcingHandler
    fun on(productAddedToCartEvent: ProductAddedToCartEvent) {
        products.add(productAddedToCartEvent.productId)
    }

    @EventSourcingHandler
    fun on(productRemovedFromCartEvent: ProductRemovedFromCartEvent) {
        products.remove(productRemovedFromCartEvent.productId)
    }

    @EventSourcingHandler
    fun on(cartAbandonedEvent: CartAbandonedEvent?) {
        markDeleted()
    }

    @EventSourcingHandler
    fun on(cartCheckedOutEvent: CartCheckedOutEvent?) {
        markDeleted()
    }

    @MessageHandlerInterceptor(messageType = CommandMessage::class)
    @Throws(
        Exception::class
    )
    fun intercept(
        message: CommandMessage<*>,
        interceptorChain: InterceptorChain
    ) {
        LOG.info("[  COMMAND ] " + message.payload.toString())
        interceptorChain.proceed()
    }

    @MessageHandlerInterceptor(messageType = EventMessage::class)
    @Throws(Exception::class)
    fun intercept(
        message: EventMessage<*>,
        interceptorChain: InterceptorChain
    ) {
        if (isLive()) {
            LOG.info("[    EVENT ] " + message.payload.toString())
        } else {
            LOG.info("[ SOURCING ] " + message.payload.toString())
        }
        interceptorChain.proceed()
    }

    companion object {
        private const val ABANDON_CART = "abandon-cart"
        private val LOG = LoggerFactory.getLogger(Cart::class.java)
    }
}
