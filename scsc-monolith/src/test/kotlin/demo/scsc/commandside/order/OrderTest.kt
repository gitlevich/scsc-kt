package demo.scsc.commandside.order

import demo.scsc.api.order
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.test.aggregate.AggregateTestFixture
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*

class OrderTest {
    private val order = AggregateTestFixture(Order::class.java)

    @Test
    fun `should publish orderCompletedEvent on completeOrderCommand`() {
        order.given(orderCreatedEvent)
            .`when`(completeOrderCommand)
            .expectEvents(orderCompletedEvent)
            .expectState { order ->
                assertThat(order.items).hasSize(1).isEqualTo(orderCreatedEvent.items)
            }
    }

    companion object {
        private val orderId = UUID.randomUUID()

        internal val orderCreatedEvent = order.OrderCreatedEvent(
            orderId = orderId,
            owner = "owner",
            items = listOf(
                order.OrderCreatedEvent.OrderItem(
                    UUID.randomUUID(),
                    "name",
                    BigDecimal.valueOf(1.0)
                )
            )
        )
        private val completeOrderCommand = order.CompleteOrderCommand(orderId)
        private val orderCompletedEvent = order.OrderCompletedEvent(orderId = orderId)
    }
}
