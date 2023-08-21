package demo.scsc.process

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
            // Note: order is significant and must match the order in which nextId() is called in the saga
            with(listOf(orderPaymentId, shipmentId).iterator()) {
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
            .expectAssociationWith("orderId", orderId.toString())
            .expectAssociationWith("shipmentId", shipmentId.toString())
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

    companion object {
        private val orderId = UUID.randomUUID()
        private val orderCreatedEvent = order.OrderCreatedEvent(
            orderId = orderId,
            owner = "John Doe",
            items = listOf(
                order.OrderCreatedEvent.OrderItem(
                    id = UUID.randomUUID(),
                    name = "Test Item",
                    price = BigDecimal("9.99")
                )
            )
        )

        private val orderPaymentId = UUID.randomUUID()
        private val requestPaymentCommand = payment.RequestPaymentCommand(
            orderPaymentId = orderPaymentId,
            orderId = orderCreatedEvent.orderId,
            amount = orderCreatedEvent.items.asSequence()
                .map { it.price }
                .reduce { acc, c -> acc.add(c) }
        )

        private val shipmentId = UUID.randomUUID()
        private val requestShipmentCommand = warehouse.RequestShipmentCommand(
            shipmentId = shipmentId,
            orderId = orderCreatedEvent.orderId,
            recipient = orderCreatedEvent.owner,
            products = orderCreatedEvent.items.asSequence()
                .map { it.id }
                .toList()
        )
    }
}
