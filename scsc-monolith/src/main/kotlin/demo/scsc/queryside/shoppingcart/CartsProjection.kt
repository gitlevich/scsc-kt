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

    @EventHandler
    fun on(event: shoppingcart.CartCreatedEvent) {
        CartStore().saveCart(event.owner, event.id)
    }

    @EventHandler
    fun on(event: shoppingcart.ProductAddedToCartEvent) {
        CartStore().saveProduct(
            event.cartId,
            event.productId
        )
    }

    @EventHandler
    fun on(event: shoppingcart.ProductRemovedFromCartEvent) {
        CartStore().removeProduct(event.cartId, event.productId)
    }

    @EventHandler
    fun on(event: shoppingcart.CartAbandonedEvent) {
        CartStore().removeCart(event.cartId)
    }

    @EventHandler
    fun on(event: shoppingcart.CartCheckoutCompletedEvent) {
        CartStore().removeCart(event.cartId)
    }

    @QueryHandler
    fun on(query: shoppingcart.GetCartQuery): shoppingcart.GetCartQuery.Response? =
        CartStore().getOwnersCarts(query.owner)

    @ResetHandler
    fun onReset() {
        LOG.info("[    RESET ] ")
        CartStore().reset()
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
