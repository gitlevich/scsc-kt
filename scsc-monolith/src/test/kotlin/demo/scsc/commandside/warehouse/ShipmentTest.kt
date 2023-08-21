package demo.scsc.commandside.warehouse

import org.axonframework.test.aggregate.AggregateTestFixture
import org.junit.jupiter.api.Test

class ShipmentTest {
    private val shipment = AggregateTestFixture(Shipment::class.java)

    @Test
    fun ` on RequestShipmentCommand, should publish ShipmentRequestedEvent`() {
        TODO("Not yet implemented")
    }

    @Test
    fun ` on ShipPackageCommand, should publish PackageShippedEvent if the package is ready`() {
        TODO("Not yet implemented")
    }

    @Test
    fun ` on ShipPackageCommand, should complain if the package is not ready`() {
        TODO("Not yet implemented")
    }

    @Test
    fun ` on PackageShippedEvent, should mark the aggregate as deleted`() {
        TODO("Not yet implemented")
    }
}
