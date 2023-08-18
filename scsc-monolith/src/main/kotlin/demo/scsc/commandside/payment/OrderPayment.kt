package demo.scsc.commandside.payment

import demo.scsc.api.payment.*
import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.eventhandling.EventMessage
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.extensions.kotlin.applyEvent
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.interceptors.MessageHandlerInterceptor
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.modelling.command.AggregateLifecycle.*
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.util.*

class OrderPayment() {
    @AggregateIdentifier
    private var orderPaymentId: UUID? = null
    private var orderId: UUID? = null
    private var requestedAmount: BigDecimal? = null
    private var paidAmount = BigDecimal.ZERO

    @CommandHandler
    constructor(requestPaymentCommand: RequestPaymentCommand): this() {

        if (requestPaymentCommand.amount <= BigDecimal.ZERO)
            throw CommandExecutionException("Can't process payments for zero or negative amounts", null)

        applyEvent(
            PaymentRequestedEvent(
                requestPaymentCommand.orderPaymentId,
                requestPaymentCommand.orderId,
                requestPaymentCommand.amount
            )
        )
    }

    @CommandHandler
    fun on(processPaymentCommand: ProcessPaymentCommand) {
        val leftToPay = requestedAmount!!.subtract(paidAmount)
        if (processPaymentCommand.amount.compareTo(leftToPay) > 0) {
            throw CommandExecutionException("Can't pay more than you own", null)
        }
        applyEvent(PaymentReceivedEvent(orderPaymentId!!, processPaymentCommand.amount))
        if (processPaymentCommand.amount.compareTo(leftToPay) == 0) {
//            apply(new OrderFullyPaidEvent(orderId, orderPaymentId));
            applyEvent(OrderFullyPaidEvent(orderPaymentId!!, orderId!!))
        }
    }

    @EventSourcingHandler
    fun on(paymentRequestedEvent: PaymentRequestedEvent) {
        orderPaymentId = paymentRequestedEvent.orderPaymentId
        orderId = paymentRequestedEvent.orderId
        requestedAmount = paymentRequestedEvent.amount
    }

    @EventSourcingHandler
    fun on(paymentReceivedEvent: PaymentReceivedEvent) {
        paidAmount = paidAmount.add(paymentReceivedEvent.amount)
    }

    @EventSourcingHandler
    fun on(orderFullyPaidEvent: OrderFullyPaidEvent?) {
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
        private val LOG = LoggerFactory.getLogger(OrderPayment::class.java)
    }
}
