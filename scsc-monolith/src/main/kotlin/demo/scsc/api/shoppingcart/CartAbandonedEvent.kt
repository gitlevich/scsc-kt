package demo.scsc.api.shoppingcart

import java.util.*

data class CartAbandonedEvent(val cartId: UUID, val reason: Reason) {
    enum class Reason {
        MANUAL,
        TIMEOUT
    }
}
