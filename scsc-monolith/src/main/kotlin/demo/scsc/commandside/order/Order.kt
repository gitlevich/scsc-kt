package demo.scsc.commandside.order

import demo.scsc.api.order.CompleteOrderCommand
import demo.scsc.api.order.OrderCompletedEvent
import demo.scsc.api.order.OrderCreatedEvent
import demo.scsc.api.order.OrderCreatedEvent.OrderItem
import demo.scsc.infra.EmailService
import jakarta.mail.MessagingException
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.eventhandling.EventMessage
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.extensions.kotlin.applyEvent
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.interceptors.MessageHandlerInterceptor
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.isLive
import org.axonframework.modelling.command.AggregateLifecycle.markDeleted
import org.axonframework.modelling.command.AggregateRoot
import org.slf4j.LoggerFactory
import java.util.*

@AggregateRoot
class Order() {
    @AggregateIdentifier
    private lateinit var orderId: UUID
    private lateinit var owner: String
    internal val items: MutableList<OrderItem> = mutableListOf()

    constructor(itemIds: List<UUID>, owner: String): this() {
        val orderItems = mutableListOf<OrderItem>()
        val productValidation = ProductValidation()
        for (itemId in itemIds) {
            val info = productValidation.forProduct(itemId)
                ?: throw IllegalStateException("No product validation available")
            check(info.forSale) { "Product ${info.name} is no longer on sale" }
            orderItems.add(OrderItem(itemId, info.name, info.price))
        }

        applyEvent(
            OrderCreatedEvent(
                orderId = UUID.randomUUID(),
                owner = owner,
                items = orderItems
            )
        )
    }

    @CommandHandler
    fun on(completeOrderCommand: CompleteOrderCommand) {
        applyEvent(OrderCompletedEvent(orderId))
    }

    @EventSourcingHandler
    fun on(orderCreatedEvent: OrderCreatedEvent) {
        orderId = orderCreatedEvent.orderId
        owner = orderCreatedEvent.owner
        items.addAll(orderCreatedEvent.items)
        if (isLive()) try {
            EmailService.sendEmail(
                owner,
                "New order $orderId",
                "Thank you for your order!\n\n$items"
            )
        } catch (e: MessagingException) {
            e.printStackTrace()
        }
    }

    @EventSourcingHandler
    fun on(orderCompletedEvent: OrderCompletedEvent) {
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
        private val LOG = LoggerFactory.getLogger(Order::class.java)
    }
}
