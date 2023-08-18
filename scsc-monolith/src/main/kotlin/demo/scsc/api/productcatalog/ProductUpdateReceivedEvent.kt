package demo.scsc.api.productcatalog

import java.math.BigDecimal
import java.util.*

data class ProductUpdateReceivedEvent(
    val id: UUID,
    val name: String,
    val desc: String,
    val price: BigDecimal,
    val image: String,
    val onSale: Boolean
)
