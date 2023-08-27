package demo.scsc.queryside.warehouse

import demo.scsc.Constants
import demo.scsc.api.warehouse
import demo.scsc.config.JpaPersistenceUnit
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.queryhandling.SimpleQueryUpdateEmitter
import org.junit.Before
import org.junit.Test
import java.util.*

class ShipmentProjectionTest {

    private val projection = ShipmentProjection()
    private val queryUpdateEmitter = SimpleQueryUpdateEmitter.builder().build()

    @Test
    fun `should persist products on ShipmentRequestedEvent`() {
        projection.on(shipmentRequestedEvent, queryUpdateEmitter)

        val shippingItems = productAddedToPackageEvents.map {
            warehouse.GetShippingQueryResponse.ShippingItem(shipmentId, it.productId, false)
        }

        assertThat(projection.shippingRequests()).isEqualTo(warehouse.GetShippingQueryResponse(shippingItems))
    }

    @Before
    fun setUp() {
        JpaPersistenceUnit.forName(Constants.SCSC)!!.newEntityManager.run {
            transaction.begin()
            createQuery("DELETE FROM ShipmentProduct").executeUpdate()
            transaction.commit()
            close()
        }
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