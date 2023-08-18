package demo.scsc.commandside.shoppingcart;

import demo.scsc.api.shoppingcart.*;
import demo.scsc.commandside.order.Order;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.interceptors.MessageHandlerInterceptor;
import org.axonframework.modelling.command.AggregateCreationPolicy;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.CreationPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;

import static org.axonframework.modelling.command.AggregateLifecycle.*;

public class Cart {

    private static final String ABANDON_CART = "abandon-cart";
    private static final Logger LOG = LoggerFactory.getLogger(Cart.class);

    @AggregateIdentifier UUID id;
    private String owner;
    private final Set<UUID> products = new HashSet<>();

    @CommandHandler
    @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
    public UUID handle(AddProductToCartCommand command, DeadlineManager deadlineManager) {

        /* -------------------------
                validation
        ------------------------- */

        if (id == null) {
            if (command.owner() == null) {
                throw new CommandExecutionException("Can't create shopping cart for unknown owner! ", null);
            }

            apply(new CartCreatedEvent(UUID.randomUUID(), command.owner()));
        }

        if (products.contains(command.productId())) {
            throw new CommandExecutionException("Product already in the cart! ", null);
        }

        /* -------------------------
                notification
        ------------------------- */

        apply(new ProductAddedToCartEvent(id, command.productId()));
        deadlineManager.schedule(Duration.ofMinutes(10), ABANDON_CART);

        return id;
    }

    @CommandHandler
    public void handle(RemoveProductFromCartCommand command) {

        /* -------------------------
                validation
        ------------------------- */

        if (!products.contains(command.productId())) {
            throw new CommandExecutionException("Product not in the cart! ", null);
        }

        /* -------------------------
                notification
        ------------------------- */

        apply(new ProductRemovedFromCartEvent(id, command.productId()));
    }

    @CommandHandler
    public void handle(AbandonCartCommand command) {

        /* -------------------------
                validation
        ------------------------- */

        /* -------------------------
                notification
        ------------------------- */

        apply(new CartAbandonedEvent(command.cartId(), CartAbandonedEvent.Reason.MANUAL));
    }

    @CommandHandler
    public void handle(CheckOutCartCommand command) {

        /* -------------------------
                validation
        ------------------------- */

        /* -------------------------
                notification
        ------------------------- */

        try {
            createNew(Order.class, () -> new Order(products.stream().toList(), owner));
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommandExecutionException(e.getMessage(), e);
        }

        apply(new CartCheckedOutEvent(command.cartId()));
    }

    @DeadlineHandler(deadlineName = ABANDON_CART)
    public void onDeadline() {
        apply(new CartAbandonedEvent(id, CartAbandonedEvent.Reason.TIMEOUT));
    }

    @EventSourcingHandler
    public void on(CartCreatedEvent cartCreatedEvent) {
        this.id = cartCreatedEvent.id();
        this.owner = cartCreatedEvent.owner();
    }

    @EventSourcingHandler
    public void on(ProductAddedToCartEvent productAddedToCartEvent) {
        this.products.add(productAddedToCartEvent.productId());
    }

    @EventSourcingHandler
    public void on(ProductRemovedFromCartEvent productRemovedFromCartEvent) {
        this.products.remove(productRemovedFromCartEvent.productId());
    }

    @EventSourcingHandler
    public void on(CartAbandonedEvent cartAbandonedEvent) {
        markDeleted();
    }

    @EventSourcingHandler
    public void on(CartCheckedOutEvent cartCheckedOutEvent) {
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
