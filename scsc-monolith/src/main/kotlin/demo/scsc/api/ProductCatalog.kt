package demo.scsc.api

import java.math.BigDecimal
import java.util.*

object ProductCatalog {
    data class ProductListQuery(val sortBy: String)
    data class ProductListQueryResponse(val products: List<ProductInfo>) {
        data class ProductInfo(val id: UUID, val name: String, val desc: String, val price: BigDecimal, val image: String)
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
