package demo.scsc.commandside.warehouse

import demo.scsc.api.warehouse
import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.eventhandling.DomainEventMessage
import org.axonframework.eventhandling.GenericDomainEventMessage
import org.axonframework.messaging.MetaData
import org.axonframework.test.aggregate.AggregateTestFixture
import org.junit.jupiter.api.Test
import java.util.*

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

    companion object {
        private val shipmentId = UUID.randomUUID()
        private val orderId = UUID.randomUUID()
        private val recipient = "John Doe"
        private val products: List<UUID> = listOf(UUID.randomUUID())
        private val requestShipmentCommand = warehouse.RequestShipmentCommand(shipmentId, orderId, recipient, products)
        private val shipPackageCommand = warehouse.ShipPackageCommand(shipmentId)
        private val shipmentRequestedEvent = warehouse.ShipmentRequestedEvent(shipmentId, recipient, products)
        private val packageShippedEvent = warehouse.PackageShippedEvent(shipmentId)
        private val addProductToPackageCommand = warehouse.AddProductToPackageCommand(shipmentId, products[0])
        private val productAddedAToPackageEvent = warehouse.ProductAddedToPackageEvent(shipmentId, products[0])
        private val packageReadyEvent = warehouse.PackageReadyEvent(shipmentId, orderId)
        private val shipmentRequestedEventMessage: DomainEventMessage<warehouse.ShipmentRequestedEvent> =
            GenericDomainEventMessage(
                Shipment::class.java.name,
                shipmentId.toString(),
                0, // sequence number
                shipmentRequestedEvent
            ).andMetaData(MetaData.with("orderId", orderId))
    }
}
