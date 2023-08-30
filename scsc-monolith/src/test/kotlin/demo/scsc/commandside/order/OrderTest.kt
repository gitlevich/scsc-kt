package demo.scsc.commandside.order

import demo.scsc.Order.completeOrderCommand
import demo.scsc.Order.orderCompletedEvent
import demo.scsc.Order.orderCreatedEvent
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
}
