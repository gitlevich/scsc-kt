package demo.scsc.commandside.order

import demo.scsc.Constants
import demo.scsc.api.ProductCatalog
import demo.scsc.config.JpaPersistenceUnit.Companion.forName
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import java.math.BigDecimal
import java.util.*

@ProcessingGroup(Constants.PROCESSING_GROUP_PRODUCT)
class ProductValidation {
    @EventHandler
    fun on(productUpdateReceivedEvent: ProductCatalog.ProductUpdateReceivedEvent) {
        val em = forName("SCSC")!!.newEntityManager
        em.transaction.begin()
        em.merge(toEntity(productUpdateReceivedEvent))
        em.transaction.commit()
    }

    fun forProduct(id: UUID?): ProductValidationInfo? {
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

    private fun toEntity(productUpdateReceivedEvent: ProductCatalog.ProductUpdateReceivedEvent): ProductValidationEntity {
        val productValidationEntity = ProductValidationEntity()
        productValidationEntity.id = productUpdateReceivedEvent.id
        productValidationEntity.name = productUpdateReceivedEvent.name
        productValidationEntity.price = productUpdateReceivedEvent.price
        productValidationEntity.isOnSale = productUpdateReceivedEvent.onSale
        return productValidationEntity
    }

    class ProductValidationInfo(productValidationEntity: ProductValidationEntity) {
        val name: String
        val price: BigDecimal
        val onSale: Boolean

        init {
            name = productValidationEntity.name
            price = productValidationEntity.price
            onSale = productValidationEntity.isOnSale
        }
    }
}
