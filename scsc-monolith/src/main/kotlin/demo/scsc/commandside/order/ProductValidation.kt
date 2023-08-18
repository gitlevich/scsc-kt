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
        val em = jpaPersistenceUnit().newEntityManager
        em.transaction.begin()
        em.merge(toEntity(productUpdateReceivedEvent))
        em.transaction.commit()
    }

    fun forProduct(id: UUID): ProductValidationInfo? = jpaPersistenceUnit()
        .newEntityManager
        .find(Product::class.java, id)
        ?.let { product -> ProductValidationInfo(product) }

    private fun toEntity(event: productCatalog.ProductUpdateReceivedEvent) = Product().apply {
        id = event.id
        name = event.name
        price = event.price
        isOnSale = event.onSale
    }

    private fun jpaPersistenceUnit() = forName("SCSC")!!

    class ProductValidationInfo(product: Product) {
        val name: String = product.name
        val price: BigDecimal = product.price
        val forSale: Boolean = product.isOnSale
    }
}
