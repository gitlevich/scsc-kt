package demo.scsc.commandside.payment;

import demo.scsc.api.payment.*;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.interceptors.MessageHandlerInterceptor;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.*;

public class OrderPayment {

    private static final Logger LOG = LoggerFactory.getLogger(OrderPayment.class);

    @AggregateIdentifier
    private UUID orderPaymentId;
    private UUID orderId;
    private BigDecimal requestedAmount;
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @SuppressWarnings("unused")
    public OrderPayment() {
        // required no-arg constructor
    }

    @CommandHandler
    public OrderPayment(RequestPaymentCommand requestPaymentCommand) {

        /* -------------------------
                validation
        ------------------------- */

        if (requestPaymentCommand.orderId() == null) {
            throw new CommandExecutionException("Can't request payment without order", null);
        }

        if (requestPaymentCommand.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CommandExecutionException("Can't process payments for zero or negative amounts", null);
        }

        /* -------------------------
                notification
        ------------------------- */

        apply(new PaymentRequestedEvent(
                requestPaymentCommand.orderPaymentId(),
                requestPaymentCommand.orderId(),
                requestPaymentCommand.amount()
        ));
    }

    @CommandHandler
    public void on(ProcessPaymentCommand processPaymentCommand) {
        BigDecimal leftToPay = requestedAmount.subtract(paidAmount);

        if (processPaymentCommand.amount().compareTo(leftToPay) > 0) {
            throw new CommandExecutionException("Can't pay more than you own", null);
        }

        apply(new PaymentReceivedEvent(orderPaymentId, processPaymentCommand.amount()));

        if (processPaymentCommand.amount().compareTo(leftToPay) == 0) {
//            apply(new OrderFullyPaidEvent(orderId, orderPaymentId));
            apply(new OrderFullyPaidEvent(orderPaymentId, orderId));

        }
    }

    @EventSourcingHandler
    public void on(PaymentRequestedEvent paymentRequestedEvent) {
        this.orderPaymentId = paymentRequestedEvent.orderPaymentId();
        this.orderId = paymentRequestedEvent.orderId();
        this.requestedAmount = paymentRequestedEvent.amount();
    }

    @EventSourcingHandler
    public void on(PaymentReceivedEvent paymentReceivedEvent) {
        this.paidAmount = this.paidAmount.add(paymentReceivedEvent.amount());
    }

    @EventSourcingHandler
    public void on(OrderFullyPaidEvent orderFullyPaidEvent) {
        markDeleted();
    }


    @MessageHandlerInterceptor(messageType = CommandMessage.class)
    public void intercept(CommandMessage<?> message,
                          InterceptorChain interceptorChain) throws Exception {
        LOG.info("[  COMMAND ] " + message.getPayload().toString());
        interceptorChain.proceed();
    }

    @MessageHandlerInterceptor(messageType = EventMessage.class)
    public void intercept(EventMessage<?> message,
                          InterceptorChain interceptorChain) throws Exception {
        if (isLive()) {
            LOG.info("[    EVENT ] " + message.getPayload().toString());
        } else {
            LOG.info("[ SOURCING ] " + message.getPayload().toString());
        }
        interceptorChain.proceed();
    }

}
