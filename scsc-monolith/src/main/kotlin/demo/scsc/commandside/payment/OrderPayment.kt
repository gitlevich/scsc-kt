package demo.scsc.commandside.payment

import demo.scsc.api.payment
import demo.scsc.api.payment.RequestPaymentCommand
import org.axonframework.commandhandling.CommandExecutionException
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
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.util.*

class OrderPayment() {
    @AggregateIdentifier
    internal lateinit var orderPaymentId: UUID
    internal lateinit var orderId: UUID
    internal lateinit var requestedAmount: BigDecimal
    internal var paidAmount = BigDecimal.ZERO

    @CommandHandler
    constructor(command: RequestPaymentCommand) : this() {
        if (command.amount <= BigDecimal.ZERO)
            throw CommandExecutionException("Can't process payments for zero or negative amounts", null)

        applyEvent(
            payment.PaymentRequestedEvent(
                command.orderPaymentId,
                command.orderId,
                command.amount
            )
        )
    }

    @CommandHandler
    fun on(command: payment.ProcessPaymentCommand) {
        val leftToPay = requestedAmount.subtract(paidAmount)
        if (command.amount > leftToPay) {
            throw CommandExecutionException("Can't pay more than you owe", null)
        }
        applyEvent(payment.PaymentReceivedEvent(orderPaymentId, command.amount))
        if (command.amount.compareTo(leftToPay) == 0) {
            applyEvent(payment.OrderFullyPaidEvent(orderPaymentId, orderId))
        }
    }

    @EventSourcingHandler
    fun on(event: payment.PaymentRequestedEvent) {
        orderPaymentId = event.orderPaymentId
        orderId = event.orderId
        requestedAmount = event.amount
    }

    @EventSourcingHandler
    fun on(event: payment.PaymentReceivedEvent) {
        paidAmount = paidAmount.add(event.amount)
    }

    @Suppress("UNUSED_PARAMETER")
    @EventSourcingHandler
    fun on(event: payment.OrderFullyPaidEvent) {
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
        private val LOG = LoggerFactory.getLogger(OrderPayment::class.java)
    }
}
