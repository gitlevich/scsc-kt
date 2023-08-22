package demo.scsc.commandside.payment

import demo.scsc.api.payment
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.test.aggregate.AggregateTestFixture
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*

class OrderPaymentTest {
    private val payment = AggregateTestFixture(OrderPayment::class.java)

    @Test
    fun `on RequestPaymentCommand, should publish PaymentRequestedEvent`() {
        payment.givenNoPriorActivity()
            .`when`(requestedPaymentCommand)
            .expectEvents(paymentRequestedEvent)
    }

    @Test
    fun `on RequestPaymentCommand, should complain if requested amount is negative`() {
        payment.givenNoPriorActivity()
            .`when`(requestedPaymentCommand.copy(amount = BigDecimal.ONE.negate()))
            .expectException(CommandExecutionException::class.java)
    }

    @Test
    fun `on ProcessPaymentCommand, should publish PaymentReceivedEvent if the amount doesn't cover requested`() {
        val insufficientAmount = BigDecimal("9.00")
        payment.given(paymentRequestedEvent)
            .`when`(processPaymentCommand.copy(amount = insufficientAmount))
            .expectEvents(paymentReceivedEvent.copy(amount = insufficientAmount))
    }

    @Test
    fun `on ProcessPaymentCommand, should publish OrderFullyPaidEvent if there is nothing left to pay`() {
        assertThat(processPaymentCommand.amount).isEqualTo(paymentRequestedEvent.amount).describedAs("precondition")

        payment.given(paymentRequestedEvent)
            .`when`(processPaymentCommand)
            .expectEvents(paymentReceivedEvent, orderFullyPaidEvent)
    }

    @Test
    fun `on ProcessPaymentCommand, should complain if requested amount is greater than amount owed`() {
        payment.given(paymentRequestedEvent)
            .`when`(processPaymentCommand.copy(amount = processPaymentCommand.amount.add(BigDecimal.ONE)))
            .expectException(CommandExecutionException::class.java)
    }

    @Test
    fun `on PaymentRequestedEvent, should set state`() {
        payment.givenNoPriorActivity()
            .`when`(requestedPaymentCommand)
            .expectState { state ->
                assert(state.orderPaymentId == paymentRequestedEvent.orderPaymentId)
                assert(state.orderId == paymentRequestedEvent.orderId)
                assert(state.requestedAmount == paymentRequestedEvent.amount)
            }
    }

    @Test
    fun `on PaymentReceivedEvent, should update paid amount`() {
        payment.given(paymentRequestedEvent)
            .`when`(processPaymentCommand)
            .expectState { state ->
                assert(state.paidAmount == paymentReceivedEvent.amount)
            }
    }

    companion object {
        private val orderId = UUID.randomUUID()
        private val requestedPaymentCommand = payment.RequestPaymentCommand(
            orderPaymentId = UUID.randomUUID(),
            orderId = orderId,
            amount = BigDecimal.TEN
        )
        private val processPaymentCommand = payment.ProcessPaymentCommand(
            orderPaymentId = requestedPaymentCommand.orderPaymentId,
            amount = requestedPaymentCommand.amount
        )
        private val paymentRequestedEvent = payment.PaymentRequestedEvent(
            orderPaymentId = requestedPaymentCommand.orderPaymentId,
            orderId = requestedPaymentCommand.orderId,
            amount = requestedPaymentCommand.amount
        )
        private val paymentReceivedEvent = payment.PaymentReceivedEvent(
            orderPaymentId = requestedPaymentCommand.orderPaymentId,
            amount = requestedPaymentCommand.amount
        )
        private val orderFullyPaidEvent = payment.OrderFullyPaidEvent(
            orderPaymentId = requestedPaymentCommand.orderPaymentId,
            orderId = requestedPaymentCommand.orderId
        )
    }
}
