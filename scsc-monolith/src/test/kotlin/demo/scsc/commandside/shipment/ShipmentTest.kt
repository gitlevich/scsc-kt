package demo.scsc.commandside.shipment

import demo.scsc.Order.orderId
import demo.scsc.Warehouse.addProductToPackageCommand
import demo.scsc.Warehouse.packageReadyEvent
import demo.scsc.Warehouse.packageShippedEvent
import demo.scsc.Warehouse.productAddedAToPackageEvent
import demo.scsc.Warehouse.requestShipmentCommand
import demo.scsc.Warehouse.shipPackageCommand
import demo.scsc.Warehouse.shipmentRequestedEvent
import demo.scsc.Warehouse.shipmentRequestedEventMessage
import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.messaging.MetaData
import org.axonframework.test.aggregate.AggregateTestFixture
import org.junit.jupiter.api.Test

class ShipmentTest {
    private val shipment = AggregateTestFixture(Shipment::class.java)

    @Test
    fun `on RequestShipmentCommand, should publish ShipmentRequestedEvent`() {
        shipment.givenNoPriorActivity()
            .`when`(requestShipmentCommand)
            .expectEvents(shipmentRequestedEvent)
    }

    @Test
    fun `on ShipPackageCommand, should publish PackageShippedEvent if the package is ready`() {
        shipment.given(shipmentRequestedEventMessage, productAddedAToPackageEvent)
            .`when`(shipPackageCommand)
            .expectEvents(packageShippedEvent)
    }

    @Test
    fun `on addProductToPackageCommand, should publish ProductAddedToPackageEvent and mark the product as ready`() {
        shipment.given(shipmentRequestedEventMessage)
            .`when`(addProductToPackageCommand)
            .expectEvents(productAddedAToPackageEvent, packageReadyEvent)
    }

    @Test
    fun `on ShipPackageCommand, should complain if the package is not ready`() {
        shipment.given(shipmentRequestedEventMessage)
            .`when`(shipPackageCommand, MetaData.with("orderId", orderId))
            .expectException(CommandExecutionException::class.java)
    }
}
