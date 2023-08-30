package demo.scsc.queryside.order

import com.typesafe.config.ConfigFactory
import demo.scsc.Order.orderCompletedEvent
import demo.scsc.Order.orderCreatedEvent
import demo.scsc.Order.orderId
import demo.scsc.Order.owner
import demo.scsc.Payment.orderFullyPaidEvent
import demo.scsc.TestScenario
import demo.scsc.Warehouse.packageReadyEvent
import demo.scsc.api.order
import demo.scsc.api.order.GetOrdersQuery
import demo.scsc.appliedTo
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.eventhandling.replay.ResetContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.stream.Stream

class OrdersProjectionTest {
    private val projection = OrdersProjection(ConfigFactory.load("application-test.conf"))

    @ParameterizedTest
    @ArgumentsSource(ScenarioProvider::class)
    fun theTest(scenario: TestScenario<*, *>) {
        scenario.appliedTo(projection)
        assertThat(projection.getOrders(getOrdersQuery)).isEqualTo(scenario.result)
    }

    @BeforeEach
    fun setUp() {
        projection.onReset(mockk(relaxed = true))
    }

    companion object {
        private val getOrdersQuery = GetOrdersQuery(owner = owner, orderId = orderId.toString())
        private val queryResponse = GetOrdersQuery.GetOrdersQueryResponse.Order(
            id = orderId,
            total = orderCreatedEvent.items.first().price.setScale(2),
            lines = orderCreatedEvent.items.map {
                GetOrdersQuery.GetOrdersQueryResponse.OrderLine(
                    name = it.name,
                    price = it.price.setScale(2)
                )
            },
            owner = owner,
            isPaid = false,
            isPrepared = false,
            isShipped = false
        )

        private class ScenarioProvider : ArgumentsProvider {
            override fun provideArguments(context: ExtensionContext): Stream<Arguments> =
                listOf(
                    TestScenario(
                        "1. should persist order on OrderCreatedEvent",
                        listOf(orderCreatedEvent),
                        GetOrdersQuery.GetOrdersQueryResponse(listOf(queryResponse))
                    ),
                    TestScenario(
                        "2. should mark order paid on OrderFullyPaidEvent",
                        listOf(orderCreatedEvent, orderFullyPaidEvent),
                        GetOrdersQuery.GetOrdersQueryResponse(listOf(queryResponse.copy(isPaid = true)))
                    ),
                    TestScenario(
                        "3. should mark order prepared PackageReadyEvent",
                        listOf(orderCreatedEvent, orderFullyPaidEvent, packageReadyEvent),
                        GetOrdersQuery.GetOrdersQueryResponse(listOf(queryResponse.copy(isPaid = true, isPrepared = true)))
                    ),
                    TestScenario(
                        "4. should mark order shipped OrderCompletedEvent",
                        listOf(orderCreatedEvent, orderFullyPaidEvent, packageReadyEvent, orderCompletedEvent),
                        GetOrdersQuery.GetOrdersQueryResponse(listOf(queryResponse.copy(isPaid = true, isPrepared = true, isShipped = true)))
                    ),
                ).stream().map { Arguments.of(it) }
        }
    }
}
