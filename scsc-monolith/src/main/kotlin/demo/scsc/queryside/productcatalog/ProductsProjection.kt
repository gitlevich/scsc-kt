package demo.scsc.queryside.productcatalog

import demo.scsc.Constants
import demo.scsc.api.productCatalog.ProductListQuery
import demo.scsc.api.productCatalog.ProductListQueryResponse
import demo.scsc.api.productCatalog.ProductListQueryResponse.ProductInfo
import demo.scsc.api.productCatalog.ProductUpdateReceivedEvent
import demo.scsc.queryside.answer
import demo.scsc.queryside.tx
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler

@ProcessingGroup(Constants.PROCESSING_GROUP_PRODUCT)
class ProductsProjection {
    @EventHandler
    fun on(productUpdateReceivedEvent: ProductUpdateReceivedEvent) {
        tx {
            if (productUpdateReceivedEvent.onSale) {
                it.merge(productUpdateReceivedEvent.toEntity())
            } else {
                val productEntity = it.find(ProductEntity::class.java, productUpdateReceivedEvent.id)
                if (productEntity != null) it.remove(productEntity)
            }
        }
    }

    @QueryHandler
    fun getProducts(query: ProductListQuery) = answer(query) {
        ProductListQueryResponse(
            it.createQuery(getProductsSql(query.sortBy), ProductEntity::class.java)
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

    private fun ProductUpdateReceivedEvent.toEntity() = ProductEntity().also {
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
