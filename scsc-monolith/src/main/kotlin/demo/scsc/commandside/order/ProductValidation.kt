package demo.scsc.commandside.order

import com.typesafe.config.Config
import demo.scsc.Constants
import demo.scsc.api.productcatalog
import demo.scsc.config.resolver.ValidatorFactory
import demo.scsc.util.tx
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import java.math.BigDecimal
import java.util.UUID

@ProcessingGroup(Constants.PROCESSING_GROUP_PRODUCT)
class ProductValidation(private val appConfig: Config): ValidatorFactory<UUID, ProductValidation.ProductValidationInfo?> {

    @EventHandler
    fun on(event: productcatalog.ProductUpdateReceivedEvent) {
        tx(appConfig) { it.merge(event.toEntity()) }
    }

    private fun productcatalog.ProductUpdateReceivedEvent.toEntity() = Product(
        id = id,
        name = name,
        price = price,
        isOnSale = onSale
    )

    class ProductValidationInfo(product: Product) {
        val name: String = product.name
        val price: BigDecimal = product.price // TODO switch to monetary amount
        val forSale: Boolean = product.isOnSale
    }

    override fun validator(subject: UUID): ProductValidationInfo? =
         tx(appConfig) {
            it.find(Product::class.java, subject)?.let { product -> ProductValidationInfo(product) }
    }
}
