package demo.scsc.commandside.order

import demo.scsc.Constants
import demo.scsc.api.productCatalog
import demo.scsc.util.tx
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import java.math.BigDecimal
import java.util.*

/**
 * What is this thing? Order construct it. Also, it seems to be a free-floating
 * event handler... Confusing, and makes Cart testing hard.
 * VG refactor this away, see [docs/qna/HowToTestExplicitAggregateCreationInCommandHandlerOfAnother.md]
 */
@ProcessingGroup(Constants.PROCESSING_GROUP_PRODUCT)
class ProductValidation {

    @EventHandler
    fun on(event: productCatalog.ProductUpdateReceivedEvent) {
        tx { it.merge(event.toEntity()) }
    }

    fun forProduct(id: UUID): ProductValidationInfo? = tx {
        it.find(Product::class.java, id)?.let { product -> ProductValidationInfo(product) }
    }

    private fun productCatalog.ProductUpdateReceivedEvent.toEntity() = Product(
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
