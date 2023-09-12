package demo.scsc.queryside.shipment

import com.typesafe.config.ConfigFactory
import demo.scsc.api.warehouse
import demo.scsc.api.warehouse.GetShippingQueryResponse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.axonframework.queryhandling.SimpleQueryUpdateEmitter
import org.junit.Test
import java.util.*

class ShipmentProjectionTest {

    private val projection = ShipmentProjection(ConfigFactory.load("application-test.conf"))
    private val queryUpdateEmitter = SimpleQueryUpdateEmitter.builder().build()

    @Test
    fun `should persist products on ShipmentRequestedEvent`() {
        projection.on(shipmentRequestedEvent, queryUpdateEmitter)

        val shippingItems = productAddedToPackageEvents.map {
            GetShippingQueryResponse.ShippingItem(shipmentId, it.productId, false)
        }

        assertThat(projection.shippingRequests()).isEqualTo(GetShippingQueryResponse(shippingItems))
    }

    @Test
    fun `should complain when asked to remove unknown product from the list on ProductAddedToPackageEvent`() {
        val unknownProductId = UUID.randomUUID()

        assertThatExceptionOfType(IllegalStateException::class.java).isThrownBy {
            projection.on(productAddedToPackageEvents.first().copy(productId = unknownProductId), queryUpdateEmitter)
        }
    }

    @Test
    fun `should remove a product from the list of products to add to shipment on ProductAddedToPackageEvent`() {
        projection.on(productAddedToPackageEvents.first(), queryUpdateEmitter)

        val shippingItems = productAddedToPackageEvents.map {
            GetShippingQueryResponse.ShippingItem(shipmentId, it.productId, false)
        }.drop(1)

        assertThat(projection.shippingRequests()).isEqualTo(GetShippingQueryResponse(shippingItems))
    }

    companion object {
        private val shipmentId = UUID.randomUUID()
        private val shipmentRequestedEvent = warehouse.ShipmentRequestedEvent(
            shipmentId = shipmentId,
            recipient = "recipient",
            products = (1..3).map { UUID.randomUUID() }
        )

        private val productAddedToPackageEvents = shipmentRequestedEvent.products.map {
            warehouse.ProductAddedToPackageEvent(shipmentId, it)
        }
    }
}
