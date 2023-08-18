package demo.scsc.api.shoppingcart

import java.util.*

data class GetCartQueryResponse(val cartId: UUID, val products: List<UUID>)
