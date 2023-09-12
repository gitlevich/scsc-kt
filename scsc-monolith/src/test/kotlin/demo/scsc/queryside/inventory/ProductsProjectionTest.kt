package demo.scsc.queryside.inventory

import com.typesafe.config.ConfigFactory
import demo.scsc.*
import demo.scsc.api.productcatalog
import demo.scsc.api.productcatalog.ProductListQuery.ProductListQueryResponse
import io.mockk.mockk
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.math.BigDecimal
import java.util.UUID
import java.util.stream.Stream

class ProductsProjectionTest {
    private val projection = ProductsProjection(ConfigFactory.load("application-test.conf"))

    @ParameterizedTest
    @ArgumentsSource(ScenarioProvider::class)
    fun theTest(scenario: TestScenario<*, *>) {
        scenario.appliedTo(projection)
        assertThat(projection.getProducts(productListQuery)).isEqualTo(scenario.result)
    }

    @BeforeEach
    fun setUp() {
        projection.onReset(mockk(relaxed = true))
    }

    companion object {
        private val productListQuery = productcatalog.ProductListQuery()
        private val product1Added = productcatalog.ProductUpdateReceivedEvent(
            id = UUID.randomUUID(),
            name = "1 Cheap Shit",
            desc = "One you would definitely not wanna wear",
            price = BigDecimal.valueOf(9.99),
            image = "https://somewhere.com/products/cheap-shirt.jpg",
            onSale = true
        )
        private val product1NameCorrected = productcatalog.ProductUpdateReceivedEvent(
            id = product1Added.id,
            name = "1 Cheap Shirt",
            desc = "One you would definitely not wanna wear",
            price = BigDecimal.valueOf(9.99),
            image = "https://somewhere.com/products/cheap-shirt.jpg",
            onSale = true
        )
        private val product2Added = productcatalog.ProductUpdateReceivedEvent(
            id = UUID.randomUUID(),
            name = "2 Blue Torn Shirt",
            desc = "One nice washed out blue",
            price = BigDecimal.valueOf(19.99),
            image = "https://somewhere.com/products/blue-shirt.jpg",
            onSale = true
        )
        private val product3Added = productcatalog.ProductUpdateReceivedEvent(
            id = UUID.randomUUID(),
            name = "3 Dirty Jeans with holes",
            desc = "Precisely what you imagine. Worn and torn by a famous individual. With signature card.",
            price = BigDecimal.valueOf(1999.99),
            image = "https://somewhere.com/products/jeans.jpg",
            onSale = true
        )
        private val product3Sold = productcatalog.ProductUpdateReceivedEvent(
            id = product3Added.id,
            name = "3 Dirty Jeans with holes",
            desc = "Precisely what you imagine. Worn and torn by a famous individual. With signature card.",
            price = BigDecimal.valueOf(1999.99),
            image = "https://somewhere.com/products/jeans.jpg",
            onSale = false
        )

        private fun productcatalog.ProductUpdateReceivedEvent.toQResp() =
            ProductListQueryResponse.ProductInfo(
                id = id,
                name = name,
                desc = desc,
                price = price,
                image = image
            )

        private class ScenarioProvider : ArgumentsProvider {
            override fun provideArguments(context: ExtensionContext): Stream<Arguments> =
                listOf(
                    TestScenario(
                        "1. should add product to inventory on ProductUpdateReceivedEvent with onSale = true",
                        listOf(product1Added, product2Added, product3Added),
                        ProductListQueryResponse(product1Added.toQResp(), product2Added.toQResp(), product3Added.toQResp())
                    ),
                    TestScenario(
                        "2. should update product name ProductUpdateReceivedEvent",
                        listOf(product1Added, product2Added, product3Added, product1NameCorrected),
                        ProductListQueryResponse(product1NameCorrected.toQResp(), product2Added.toQResp(), product3Added.toQResp())
                    ),
                    TestScenario(
                        "2. should remove product from inventory on ProductUpdateReceivedEvent with onSale = false",
                        listOf(product1Added, product2Added, product3Added, product1NameCorrected, product3Sold),
                        ProductListQueryResponse(product1NameCorrected.toQResp(), product2Added.toQResp())
                    ),
                ).stream().map { Arguments.of(it) }
        }
    }

}

