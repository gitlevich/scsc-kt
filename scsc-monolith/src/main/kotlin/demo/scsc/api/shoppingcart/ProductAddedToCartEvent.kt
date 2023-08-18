package demo.scsc.api.shoppingcart

import java.util.*

data class ProductAddedToCartEvent(val cartId: UUID, val productId: UUID)
