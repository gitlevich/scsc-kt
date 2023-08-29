package demo.thirdparty.inventory

import java.math.BigDecimal
import java.util.*

data class InventoryProduct(
    val id: UUID,
    val name: String,
    val desc: String,
    val price: BigDecimal,
    val image: String,
    val onSale: Boolean = false
)
