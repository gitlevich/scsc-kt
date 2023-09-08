package demo.scsc.api

import java.math.BigDecimal
import java.util.*

@Suppress("ClassName", "SpellCheckingInspection")
object productcatalog {

    data class ProductListQuery(val sortBy: SortBy = SortBy.NAME) {
        enum class SortBy(val value: String) {
            NAME("name"), PRICE("price")
        }
        data class ProductListQueryResponse(val products: List<ProductInfo>) {
            constructor(vararg products: ProductInfo) : this(products.toList())
            data class ProductInfo(val id: UUID, val name: String, val desc: String, val price: BigDecimal, val image: String)
        }
    }

    data class ProductUpdateReceivedEvent(
        val id: UUID,
        val name: String,
        val desc: String,
        val price: BigDecimal,
        val image: String,
        val onSale: Boolean
    )
}
