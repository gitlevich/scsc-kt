package demo.scsc.queryside.shoppingcart

import demo.scsc.Constants
import demo.scsc.api.shoppingcart
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.EventMessage
import org.axonframework.eventhandling.ResetHandler
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.interceptors.MessageHandlerInterceptor
import org.axonframework.queryhandling.QueryHandler
import org.slf4j.LoggerFactory

@ProcessingGroup(Constants.PROCESSING_GROUP_CART)
class CartsProjection {

    private val cartStore = CartStore()

    @QueryHandler
    fun handle(query: shoppingcart.GetCartQuery): shoppingcart.GetCartQuery.Response? =
        cartStore.getOwnersCarts(query.owner)

    @EventHandler
    fun on(event: shoppingcart.CartCreatedEvent) {
        cartStore.saveCart(event.owner, event.id)
    }

    @EventHandler
    fun on(event: shoppingcart.ProductAddedToCartEvent) {
        cartStore.saveProduct(
            cartId = event.cartId,
            productId = event.productId
        )
    }

    @EventHandler
    fun on(event: shoppingcart.ProductRemovedFromCartEvent) {
        cartStore.removeProduct(event.cartId, event.productId)
    }

    @EventHandler
    fun on(event: shoppingcart.CartAbandonedEvent) {
        cartStore.removeCart(event.cartId)
    }

    @EventHandler
    fun on(event: shoppingcart.CartCheckoutCompletedEvent) {
        cartStore.removeCart(event.cartId)
    }

    @ResetHandler
    fun onReset() {
        LOG.info("[    RESET ] ")
        cartStore.reset()
    }

    @MessageHandlerInterceptor(messageType = EventMessage::class)
    fun intercept(message: EventMessage<*>, interceptorChain: InterceptorChain) {
        LOG.info("[    EVENT ] " + message.payload.toString())
        interceptorChain.proceed()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(CartsProjection::class.java)
    }
}
