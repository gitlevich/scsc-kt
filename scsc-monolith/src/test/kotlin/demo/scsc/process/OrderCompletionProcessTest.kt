package demo.scsc.process

import demo.scsc.Order.completeOrderCommand
import demo.scsc.Order.createOrderCommand
import demo.scsc.Order.orderCreatedEvent
import demo.scsc.Payment.orderFullyPaidEvent
import demo.scsc.Warehouse.packageReadyEvent
import demo.scsc.Warehouse.packageShippedEvent
import demo.scsc.Warehouse.requestPaymentCommand
import demo.scsc.Warehouse.requestShipmentCommand
import demo.scsc.Warehouse.shipPackageCommand
import demo.scsc.api.order
import demo.scsc.api.payment
import demo.scsc.api.warehouse
import io.mockk.every
import io.mockk.mockk
import org.axonframework.test.saga.SagaTestFixture
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*

class OrderCompletionProcessTest {
    private val process = SagaTestFixture(OrderCompletionProcess::class.java)

    @BeforeEach
    fun setUp() {
        val nextUuid: () -> UUID = mockk<() -> UUID>(relaxed = true).also {
            // Note: order is significant and must match the order in which nextId() is called in the saga's
            // on(OrderCreatedEvent) handler
            with(listOf(requestPaymentCommand.orderPaymentId, requestShipmentCommand.shipmentId).iterator()) {
                every { it.invoke() } returns next() andThen next()
            }
        }
        process.registerResource(nextUuid)
    }


    @Test
    fun `should start saga on OrderCreatedEvent`() {
        process.givenNoPriorActivity()
            .whenPublishingA(orderCreatedEvent)
            .expectActiveSagas(1)
    }

    @Test
    fun `should associate saga with order id and shipment id on OrderCreatedEvent`() {
        process.givenNoPriorActivity()
            .whenPublishingA(orderCreatedEvent)
            .expectAssociationWith("orderId", createOrderCommand.orderId.toString())
            .expectAssociationWith("shipmentId", requestShipmentCommand.shipmentId.toString())
    }

    @Test
    fun `should dispatch commands on OrderCreatedEvent`() {
        process.givenNoPriorActivity()
            .whenPublishingA(orderCreatedEvent)
            .expectDispatchedCommands(
                requestPaymentCommand,
                requestShipmentCommand
            )
    }

    @Test
    fun `should dispatch ShipPackageCommand on PackageReadyEven if OrderFullyPaidEvent has been received`() {
        process.givenAPublished(orderCreatedEvent)
            .andThenAPublished(orderFullyPaidEvent)
            .whenPublishingA(packageReadyEvent)
            .expectDispatchedCommands(shipPackageCommand)
    }

    @Test
    fun `should not dispatch ShipPackageCommand on PackageReadyEven if order has not been fully paid`() {
        process.givenAPublished(orderCreatedEvent)
            .whenPublishingA(packageReadyEvent)
            .expectNoDispatchedCommands()
    }

    @Test
    fun `should not dispatch ShipPackageCommand on OrderFullyPaidEvent if package is not ready`() {
        process.givenAPublished(orderCreatedEvent)
            .whenPublishingA(orderFullyPaidEvent)
            .expectNoDispatchedCommands()
    }

    @Test
    fun `should dispatch ShipPackageCommand on OrderFullyPaidEvent if PackageReadyEven has been received`() {
        process.givenAPublished(orderCreatedEvent)
            .andThenAPublished(packageReadyEvent)
            .whenPublishingA(orderFullyPaidEvent)
            .expectDispatchedCommands(shipPackageCommand)
    }

    @Test
    fun `should dispatch CompleteOrderCommand on PackageShippedEvent`() {
        process.givenAPublished(orderCreatedEvent)
            .andThenAPublished(orderFullyPaidEvent)
            .andThenAPublished(packageReadyEvent)
            .whenPublishingA(packageShippedEvent)
            .expectDispatchedCommands(completeOrderCommand)
    }
}
