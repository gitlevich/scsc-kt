package demo.scsc.queryside.payment

import com.typesafe.config.ConfigFactory
import demo.scsc.Order.orderId
import demo.scsc.Payment.paymentReceivedEvent
import demo.scsc.Payment.paymentRequestedEvent
import demo.scsc.TestScenario
import demo.scsc.api.payment
import demo.scsc.appliedTo
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.math.BigDecimal
import java.util.stream.Stream

class PaymentProjectionTest {
    private val projection = PaymentProjection(ConfigFactory.load("application-test.conf"))

    @ParameterizedTest
    @ArgumentsSource(ScenarioProvider::class)
    fun theTest(scenario: TestScenario<*, *>) {
        scenario.appliedTo(projection)
        assertThat(projection.handle(query)).isEqualTo(scenario.result)
    }

    @BeforeEach
    fun setUp() {
        projection.onReset(mockk(relaxed = true))
    }

    companion object {
        private val query = payment.GetPaymentForOrderQuery(orderId = orderId)
        private class ScenarioProvider : ArgumentsProvider {
            override fun provideArguments(context: ExtensionContext): Stream<Arguments> =
                listOf(
                    TestScenario(
                        "1. should persist payment on PaymentRequestedEvent",
                        listOf(paymentRequestedEvent),
                        payment.GetPaymentForOrderQuery.Response(
                            id = paymentRequestedEvent.orderPaymentId,
                            orderId = paymentRequestedEvent.orderId,
                            requestedAmount = paymentRequestedEvent.amount.setScale(2),
                            paidAmount = BigDecimal.ZERO.setScale(2)
                        )
                    ),
                    TestScenario(
                        "2. should note received payment on PaymentReceivedEvent",
                        listOf(paymentRequestedEvent, paymentReceivedEvent),
                        payment.GetPaymentForOrderQuery.Response(
                            id = paymentRequestedEvent.orderPaymentId,
                            orderId = paymentRequestedEvent.orderId,
                            requestedAmount = paymentRequestedEvent.amount.setScale(2),
                            paidAmount = paymentReceivedEvent.amount.setScale(2)
                        )
                    ),
                ).stream().map { Arguments.of(it) }
        }
    }
}
