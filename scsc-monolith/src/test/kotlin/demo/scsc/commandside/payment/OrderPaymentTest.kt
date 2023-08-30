package demo.scsc.commandside.payment

import demo.scsc.Payment.orderFullyPaidEvent
import demo.scsc.Payment.paymentReceivedEvent
import demo.scsc.Payment.paymentRequestedEvent
import demo.scsc.Payment.processPaymentCommand
import demo.scsc.Payment.requestedPaymentCommand
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.test.aggregate.AggregateTestFixture
import org.junit.jupiter.api.Test
import java.math.BigDecimal

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
}
