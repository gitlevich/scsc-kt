package demo.scsc.queryside.productcatalog

import demo.scsc.Constants
import demo.scsc.api.ProductCatalog.ProductListQuery
import demo.scsc.api.ProductCatalog.ProductListQueryResponse
import demo.scsc.api.ProductCatalog.ProductListQueryResponse.ProductInfo
import demo.scsc.api.ProductCatalog.ProductUpdateReceivedEvent
import demo.scsc.config.JpaPersistenceUnit.Companion.forName
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import java.util.stream.Collectors

@ProcessingGroup(Constants.PROCESSING_GROUP_PRODUCT)
class ProductsProjection {
    @EventHandler
    fun on(productUpdateReceivedEvent: ProductUpdateReceivedEvent) {
        val em = forName("SCSC")!!.newEntityManager
        em.transaction.begin()
        if (productUpdateReceivedEvent.onSale) {
            em.merge(toEntity(productUpdateReceivedEvent))
        } else {
            val productEntity = em.find(ProductEntity::class.java, productUpdateReceivedEvent.id)
            if (productEntity != null) em.remove(productEntity)
        }
        em.transaction.commit()
    }

    @QueryHandler
    fun getProducts(query: ProductListQuery): ProductListQueryResponse {
        val em = forName("SCSC")!!.newEntityManager
        em.transaction.begin()
        val products = em.createQuery(getProductsSql(query.sortBy), ProductEntity::class.java).resultList
        val response = ProductListQueryResponse(
            products.stream()
                .map { productEntity: ProductEntity ->
                    ProductInfo(
                        productEntity.id!!,
                        productEntity.name!!,
                        productEntity.desc!!,
                        productEntity.price!!,
                        productEntity.image!!
                    )
                }
                .collect(Collectors.toList())
        )
        em.transaction.commit()
        return response
    }

    private fun toEntity(productUpdateReceivedEvent: ProductUpdateReceivedEvent): ProductEntity {
        val productEntity = ProductEntity()
        productEntity.id = productUpdateReceivedEvent.id
        productEntity.name = productUpdateReceivedEvent.name
        productEntity.desc = productUpdateReceivedEvent.desc
        productEntity.price = productUpdateReceivedEvent.price
        productEntity.image = productUpdateReceivedEvent.image
        return productEntity
    }

    companion object {
        private const val GET_PRODUCTS_SQL = "SELECT p FROM ProductEntity AS p"
        private fun getProductsSql(sortBy: String): String {
            return GET_PRODUCTS_SQL + " ORDER BY " + sortBy
        }
    }
}
