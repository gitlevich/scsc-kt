package demo.scsc.commandside.order

import demo.scsc.Constants
import demo.scsc.api.productCatalog
import demo.scsc.config.JpaPersistenceUnit.Companion.forName
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import java.math.BigDecimal
import java.util.*

@ProcessingGroup(Constants.PROCESSING_GROUP_PRODUCT)
class ProductValidation {

    @EventHandler
    fun on(productUpdateReceivedEvent: productCatalog.ProductUpdateReceivedEvent) {
        val em = forName("SCSC")!!.newEntityManager
        em.transaction.begin()
        em.merge(toEntity(productUpdateReceivedEvent))
        em.transaction.commit()
    }

    fun forProduct(id: UUID): ProductValidationInfo? {
        var productValidationInfo: ProductValidationInfo? = null
        val em = forName("SCSC")!!.newEntityManager
        val productValidationEntity = em.find(
            ProductValidationEntity::class.java, id
        )
        if (productValidationEntity != null) {
            productValidationInfo = ProductValidationInfo(productValidationEntity)
        }
        return productValidationInfo
    }

    private fun toEntity(event: productCatalog.ProductUpdateReceivedEvent): ProductValidationEntity {
        val productValidationEntity = ProductValidationEntity()
        productValidationEntity.id = event.id
        productValidationEntity.name = event.name
        productValidationEntity.price = event.price
        productValidationEntity.isOnSale = event.onSale
        return productValidationEntity
    }

    class ProductValidationInfo(productValidationEntity: ProductValidationEntity) {
        val name: String
        val price: BigDecimal
        val forSale: Boolean

        init {
            name = productValidationEntity.name
            price = productValidationEntity.price
            forSale = productValidationEntity.isOnSale
        }
    }
}
