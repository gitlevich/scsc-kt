package demo.scsc.commandside.order;

import demo.scsc.api.order.CompleteOrderCommand;
import demo.scsc.api.order.OrderCompletedEvent;
import demo.scsc.api.order.OrderCreatedEvent;
import demo.scsc.infra.EmailService;
import jakarta.mail.MessagingException;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.interceptors.MessageHandlerInterceptor;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.*;

public class Order {

    private static final Logger LOG = LoggerFactory.getLogger(Order.class);

    @AggregateIdentifier
    private UUID orderId;
    private String owner;
    private List<OrderCreatedEvent.OrderItem> items;

    @SuppressWarnings("unused")
    public Order() {
        // required no-arg constructor
    }

    public Order(List<UUID> itemIds, String owner) {

        /* -------------------------
                validation
        ------------------------- */

        List<OrderCreatedEvent.OrderItem> orderItems = new LinkedList<>();
        ProductValidation productValidation = new ProductValidation();
        for (UUID itemId : itemIds) {
            ProductValidation.ProductValidationInfo info = productValidation.forProduct(itemId);
            if (info == null) {
                throw new IllegalStateException("No product validation available");
            }

            if (!info.onSale) {
                throw new IllegalStateException("Product " + info.name + " is no longer on sale");
            }

            orderItems.add(new OrderCreatedEvent.OrderItem(itemId, info.name, info.price));
        }

        /* -------------------------
                notification
        ------------------------- */

        AggregateLifecycle.apply(
                new OrderCreatedEvent(
                        UUID.randomUUID(),
                        owner,
                        orderItems
                )
        );
    }

    @CommandHandler
    public void on(CompleteOrderCommand completeOrderCommand) {

        /* -------------------------
                validation
        ------------------------- */


        /* -------------------------
                notification
        ------------------------- */

        apply(new OrderCompletedEvent(orderId));
    }


    @EventSourcingHandler
    public void on(OrderCreatedEvent orderCreatedEvent) {
        this.orderId = orderCreatedEvent.orderId();
        this.owner = orderCreatedEvent.owner();
        this.items = orderCreatedEvent.items();

        if (isLive()) {
            try {
                EmailService.sendEmail(
                        owner,
                        "New order " + orderId,
                        "Thank you for your order!\n\n" + items
                );
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }

    @EventSourcingHandler
    public void on(OrderCompletedEvent orderCompletedEvent) {
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
