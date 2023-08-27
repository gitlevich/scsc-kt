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
        private val createOrderCommand = order.CreateOrderCommand(
            owner = "owner",
            itemIds = listOf(UUID.randomUUID())
        )
        private val orderCreatedEvent = order.OrderCreatedEvent(
            orderId = orderId,
            owner = createOrderCommand.owner,
            items = createOrderCommand.itemIds.map {
                order.OrderCreatedEvent.OrderItem(it, "name", BigDecimal.valueOf(1.0))
            }
        )
        private val completeOrderCommand = order.CompleteOrderCommand(orderId)
        private val orderCompletedEvent = order.OrderCompletedEvent(orderId = orderId)
    }
}
