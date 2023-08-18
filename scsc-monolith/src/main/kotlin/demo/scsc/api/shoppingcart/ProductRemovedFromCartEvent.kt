package demo.scsc.api.shoppingcart

import java.util.*

data class ProductRemovedFromCartEvent(val cartId: UUID, val productId: UUID)
