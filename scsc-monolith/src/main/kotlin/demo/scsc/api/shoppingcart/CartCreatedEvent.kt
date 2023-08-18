package demo.scsc.api.shoppingcart

import java.util.*

data class CartCreatedEvent(val id: UUID, val owner: String)
