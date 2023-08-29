package demo.scsc.queryside.productcatalog

import demo.scsc.Constants
import demo.scsc.api.productcatalog.ProductListQuery
import demo.scsc.api.productcatalog.ProductListQueryResponse
import demo.scsc.api.productcatalog.ProductListQueryResponse.ProductInfo
import demo.scsc.api.productcatalog.ProductUpdateReceivedEvent
import demo.scsc.util.answer
import demo.scsc.util.tx
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler

@ProcessingGroup(Constants.PROCESSING_GROUP_PRODUCT)
class ProductsProjection {

    @EventHandler
    fun on(productUpdateReceivedEvent: ProductUpdateReceivedEvent) {
        tx { em ->
            if (productUpdateReceivedEvent.onSale) em.merge(productUpdateReceivedEvent.toEntity()) else
                em.find(CatalogProduct::class.java, productUpdateReceivedEvent.id)?.let { em.remove(it) }
        }
    }

    @QueryHandler
    fun getProducts(query: ProductListQuery) = answer(query) {
        ProductListQueryResponse(
            it.createQuery(getProductsSql(query.sortBy), CatalogProduct::class.java)
                .resultList
                .asSequence()
                .map { productEntity ->
                    ProductInfo(
                        productEntity.id!!,
                        productEntity.name!!,
                        productEntity.desc!!,
                        productEntity.price!!,
                        productEntity.image!!
                    )
                }
                .toList()
        )
    }

    private fun ProductUpdateReceivedEvent.toEntity() = CatalogProduct().also {
        it.id = id
        it.name = name
        it.desc = desc
        it.price = price
        it.image = image
    }

    companion object {
        private const val GET_PRODUCTS_SQL = "SELECT p FROM ProductEntity AS p"
        private fun getProductsSql(sortBy: String): String = "$GET_PRODUCTS_SQL ORDER BY $sortBy"
    }
}
