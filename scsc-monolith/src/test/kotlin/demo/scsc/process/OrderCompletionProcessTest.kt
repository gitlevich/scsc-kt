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
        val ids = listOf(orderPaymentId, shipmentId).iterator()
        val nextUuid: () -> UUID = mockk<() -> UUID>(relaxed = true).also {
            every { it.invoke() } returns ids.next() andThen ids.next()
        }
        process.registerResource(nextUuid)
    }

    @Test
    fun `should start saga on order created event`() {
        process.givenNoPriorActivity()
            .whenPublishingA(orderCreatedEvent)
            .expectActiveSagas(1)
            .expectAssociationWith("orderId", orderCreatedEvent.orderId.toString())
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
                    price = BigDecimal.TEN
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
