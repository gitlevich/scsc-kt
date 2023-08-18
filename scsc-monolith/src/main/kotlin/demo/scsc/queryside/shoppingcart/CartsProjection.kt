package demo.scsc.queryside.shoppingcart

import demo.scsc.Constants
import demo.scsc.api.shoppingcart.*
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.EventMessage
import org.axonframework.eventhandling.ResetHandler
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.interceptors.MessageHandlerInterceptor
import org.axonframework.queryhandling.QueryHandler
import org.slf4j.LoggerFactory
import java.util.*

@ProcessingGroup(Constants.PROCESSING_GROUP_CART)
class CartsProjection {
    @EventHandler
    fun on(cartCreatedEvent: CartCreatedEvent) {
        val cartStore = CartStore()
        cartStore.saveCart(cartCreatedEvent.owner, cartCreatedEvent.id)
    }

    @EventHandler
    fun on(productAddedToCartEvent: ProductAddedToCartEvent) {
        val cartStore = CartStore()
        cartStore.saveProduct(
            productAddedToCartEvent.cartId,
            productAddedToCartEvent.productId
        )
    }

    @EventHandler
    fun on(productRemovedFromCartEvent: ProductRemovedFromCartEvent) {
        val cartStore = CartStore()
        cartStore.removeProduct(productRemovedFromCartEvent.cartId, productRemovedFromCartEvent.productId)
    }

    @EventHandler
    fun on(cartAbandonedEvent: CartAbandonedEvent) {
        val cartStore = CartStore()
        cartStore.removeCart(cartAbandonedEvent.cartId)
    }

    @EventHandler
    fun on(cartCheckedOutEvent: CartCheckedOutEvent) {
        val cartStore = CartStore()
        cartStore.removeCart(cartCheckedOutEvent.cartId)
    }

    @QueryHandler
    fun on(getCartQuery: GetCartQuery): Optional<GetCartQueryResponse> {
        val cartStore = CartStore()
        val getCartQueryResponse = cartStore.getOwnersCarts(getCartQuery.owner)
        return Optional.ofNullable(getCartQueryResponse)
    }

    @ResetHandler
    fun onReset() {
        LOG.info("[    RESET ] ")
        val cartStore = CartStore()
        cartStore.reset()
    }

    @MessageHandlerInterceptor(messageType = EventMessage::class)
    fun intercept(
        message: EventMessage<*>,
        interceptorChain: InterceptorChain
    ) {
        LOG.info("[    EVENT ] " + message.payload.toString())
        interceptorChain.proceed()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(CartsProjection::class.java)
    }
}