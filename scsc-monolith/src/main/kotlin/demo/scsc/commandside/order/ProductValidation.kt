package demo.scsc.commandside.order

import demo.scsc.Constants
import demo.scsc.api.productcatalog
import demo.scsc.util.tx
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import java.math.BigDecimal
import java.util.*

@ProcessingGroup(Constants.PROCESSING_GROUP_PRODUCT)
class ProductValidation {

    @EventHandler
    fun on(event: productcatalog.ProductUpdateReceivedEvent) {
        tx { it.merge(event.toEntity()) }
    }

    fun forProduct(id: UUID): ProductValidationInfo? = tx {
        it.find(Product::class.java, id)?.let { product -> ProductValidationInfo(product) }
    }

    private fun productcatalog.ProductUpdateReceivedEvent.toEntity() = Product(
        id = id,
        name = name,
        price = price,
        isOnSale = onSale
    )

    class ProductValidationInfo(product: Product) {
        val name: String = product.name
        val price: BigDecimal = product.price
        val forSale: Boolean = product.isOnSale
    }
}
