package demo.scsc.queryside.productcatalog

import com.typesafe.config.Config
import demo.scsc.Constants
import demo.scsc.api.productcatalog.ProductListQuery
import demo.scsc.api.productcatalog.ProductListQuery.ProductListQueryResponse
import demo.scsc.api.productcatalog.ProductUpdateReceivedEvent
import demo.scsc.util.answer
import demo.scsc.util.tx
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.ResetHandler
import org.axonframework.eventhandling.replay.ResetContext
import org.axonframework.queryhandling.QueryHandler

@ProcessingGroup(Constants.PROCESSING_GROUP_PRODUCT)
class ProductsProjection(private val appConfig: Config) {

    @EventHandler
    fun on(productUpdateReceivedEvent: ProductUpdateReceivedEvent) {
        tx(appConfig) { em ->
            if (productUpdateReceivedEvent.onSale) em.merge(productUpdateReceivedEvent.toEntity()) else
                em.find(CatalogProduct::class.java, productUpdateReceivedEvent.id)?.let { em.remove(it) }
        }
    }

    @QueryHandler
    fun getProducts(query: ProductListQuery) = answer(query, appConfig) {
        ProductListQueryResponse(
            it.createQuery(getProductsSql(query.sortBy), CatalogProduct::class.java)
                .resultList
                .asSequence()
                .map { productEntity ->
                    ProductListQueryResponse.ProductInfo(
                        id = productEntity.id,
                        name = productEntity.name,
                        desc = productEntity.desc,
                        price = productEntity.price,
                        image = productEntity.image
                    )
                }
                .toList()
        )
    }

    @ResetHandler
    fun onReset(resetContext: ResetContext<*>?) {
        tx(appConfig) { it.createQuery("DELETE FROM CatalogProduct").executeUpdate() }
    }

    private fun ProductUpdateReceivedEvent.toEntity() = CatalogProduct(
        id = id,
        name = name,
        desc = desc,
        price = price,
        image = image
    )

    companion object {
        private const val GET_PRODUCTS_SQL = "SELECT p FROM CatalogProduct AS p"
        private fun getProductsSql(sortBy: ProductListQuery.SortBy): String = "$GET_PRODUCTS_SQL ORDER BY ${sortBy.value}"
    }
}
