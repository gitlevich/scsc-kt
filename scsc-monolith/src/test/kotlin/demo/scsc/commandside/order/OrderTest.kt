package demo.scsc.commandside.order

import demo.scsc.Order.completeOrderCommand
import demo.scsc.Order.createOrderCommand
import demo.scsc.Order.orderCompletedEvent
import demo.scsc.Order.orderCreatedEvent
import demo.scsc.config.resolver.Finder
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.test.aggregate.AggregateTestFixture
import org.junit.jupiter.api.Test
import java.util.*

class OrderTest {
    private val productValidator = mockk<ProductService.ProductDescription>(relaxed = true)
        .also { validator ->
            every { validator.name } returns orderCreatedEvent.items.first().name
            every { validator.price } returns orderCreatedEvent.items.first().price
            every { validator.inStock } returns true
        }

    private val order = AggregateTestFixture(Order::class.java).also {
        it.registerInjectableResource(
            object : Finder<UUID, ProductService.ProductDescription?> {
                override fun invoke(subject: UUID): ProductService.ProductDescription = productValidator
            }
        )
    }

    @Test
    fun `should create order on CreateOrderCommand`() {
        order.givenNoPriorActivity()
            .`when`(createOrderCommand)
            .expectEvents(orderCreatedEvent)
            .expectState { order ->
                assertThat(order.items).hasSize(1).isEqualTo(orderCreatedEvent.items)
            }
    }

    @Test
    fun `should refuse to create order when an order item is no longer for sale on CreateOrderCommand`() {
        every { productValidator.inStock } returns false

        order.givenNoPriorActivity()
            .`when`(createOrderCommand)
            .expectException(IllegalStateException::class.java)
    }

    @Test
    fun `should publish orderCompletedEvent on completeOrderCommand`() {
        order.given(orderCreatedEvent)
            .`when`(completeOrderCommand)
            .expectEvents(orderCompletedEvent)
            .expectState { order ->
                assertThat(order.items).hasSize(1).isEqualTo(orderCreatedEvent.items)
            }
    }

    @Test
    fun `should create order on CompleteOrderCommand`() {
        order.givenNoPriorActivity()
            .`when`(createOrderCommand)
            .expectEvents(orderCreatedEvent)
            .expectState { order ->
                assertThat(order.items).hasSize(1).isEqualTo(orderCreatedEvent.items)
            }
    }
}
