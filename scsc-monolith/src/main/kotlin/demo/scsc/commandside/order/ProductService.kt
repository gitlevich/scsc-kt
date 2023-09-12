package demo.scsc.commandside.order

import com.typesafe.config.Config
import demo.scsc.Constants
import demo.scsc.api.productcatalog
import demo.scsc.config.resolver.Finder
import demo.scsc.util.tx
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import java.math.BigDecimal
import java.util.*

@ProcessingGroup(Constants.PROCESSING_GROUP_PRODUCT)
class ProductService(private val appConfig: Config) : Finder<UUID, ProductService.ProductDescription?> {

    @EventHandler
    fun on(event: productcatalog.ProductUpdateReceivedEvent) {
        tx(appConfig) { it.merge(event.toEntity()) }
    }

    private fun productcatalog.ProductUpdateReceivedEvent.toEntity() = Product(
        id = id,
        name = name,
        price = price,
        isInStock = onSale
    )

    class ProductDescription(product: Product) {
        val name: String = product.name
        val price: BigDecimal = product.price // TODO switch to monetary amount
        val inStock: Boolean = product.isInStock
    }

    override fun invoke(subject: UUID): ProductDescription? = tx(appConfig) {
        it.find(Product::class.java, subject)?.let { product -> ProductDescription(product) }
    }
}
