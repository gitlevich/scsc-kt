package demo.scsc.commandside.order

import demo.scsc.Constants
import demo.scsc.api.productCatalog
import demo.scsc.util.tx
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import java.math.BigDecimal
import java.util.*

@ProcessingGroup(Constants.PROCESSING_GROUP_PRODUCT)
class ProductValidation {

    @EventHandler
    fun on(productUpdateReceivedEvent: productCatalog.ProductUpdateReceivedEvent) {
        tx { it.merge(productUpdateReceivedEvent.toEntity()) }
    }

    fun forProduct(id: UUID): ProductValidationInfo? = tx {
        it.find(Product::class.java, id)?.let { product -> ProductValidationInfo(product) }
    }

    private fun productCatalog.ProductUpdateReceivedEvent.toEntity() =
        Product().let {
            it.id = id
            it.name = name
            it.price = price
            it.isOnSale = onSale
        }

    class ProductValidationInfo(product: Product) {
        val name: String = product.name
        val price: BigDecimal = product.price
        val forSale: Boolean = product.isOnSale
    }
}
