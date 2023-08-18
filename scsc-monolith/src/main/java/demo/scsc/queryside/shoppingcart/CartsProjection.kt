package demo.scsc.queryside.shoppingcart;

import demo.scsc.Constants;
import demo.scsc.api.shoppingcart.*;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.ResetHandler;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.interceptors.MessageHandlerInterceptor;
import org.axonframework.queryhandling.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@ProcessingGroup(Constants.PROCESSING_GROUP_CART)
public class CartsProjection {

    private static final Logger LOG = LoggerFactory.getLogger(CartsProjection.class);

    @EventHandler
    public void on(CartCreatedEvent cartCreatedEvent) {
        CartStore cartStore = new CartStore();
        cartStore.saveCart(cartCreatedEvent.owner(), cartCreatedEvent.id());
    }

    @EventHandler
    public void on(ProductAddedToCartEvent productAddedToCartEvent) {
        CartStore cartStore = new CartStore();
        cartStore.saveProduct(
                productAddedToCartEvent.cartId(),
                productAddedToCartEvent.productId()
        );
    }

    @EventHandler
    public void on(ProductRemovedFromCartEvent productRemovedFromCartEvent) {
        CartStore cartStore = new CartStore();
        cartStore.removeProduct(productRemovedFromCartEvent.cartId(), productRemovedFromCartEvent.productId());
    }

    @EventHandler
    public void on(CartAbandonedEvent cartAbandonedEvent) {
        CartStore cartStore = new CartStore();
        cartStore.removeCart(cartAbandonedEvent.cartId());
    }

    @EventHandler
    public void on(CartCheckedOutEvent cartCheckedOutEvent) {
        CartStore cartStore = new CartStore();
        cartStore.removeCart(cartCheckedOutEvent.cartId());
    }

    @QueryHandler
    public Optional<GetCartQueryResponse> on(GetCartQuery getCartQuery) {

        CartStore cartStore = new CartStore();
        GetCartQueryResponse getCartQueryResponse = cartStore.getOwnersCarts(getCartQuery.owner());
        return Optional.ofNullable(getCartQueryResponse);
    }

    @ResetHandler
    public void onReset() {
        LOG.info("[    RESET ] ");
        CartStore cartStore = new CartStore();
        cartStore.reset();
    }

    @MessageHandlerInterceptor(messageType = EventMessage.class)
    public void intercept(EventMessage<?> message,
                          InterceptorChain interceptorChain) throws Exception {
        LOG.info("[    EVENT ] " + message.getPayload().toString());
        interceptorChain.proceed();
    }

}
