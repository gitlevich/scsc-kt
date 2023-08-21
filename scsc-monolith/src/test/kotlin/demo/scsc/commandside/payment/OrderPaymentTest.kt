package demo.scsc.commandside.payment

import org.axonframework.test.aggregate.AggregateTestFixture
import org.junit.jupiter.api.Test

class OrderPaymentTest {
    private val payment = AggregateTestFixture(OrderPayment::class.java)

    @Test
    fun `on RequestPaymentCommand, should publish PaymentRequestedEvent`() {
        TODO("Not yet implemented")
    }

    @Test
    fun ` on RequestPaymentCommand, should complain if requested amount is negative`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `on ProcessPaymentCommand, should publish PaymentReceivedEvent`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `on ProcessPaymentCommand, should publish OrderFullyPaidEvent if there is nothing left to pay`() {
        TODO("Not yet implemented")
    }

    @Test
    fun ` on ProcessPaymentCommand, should complain if requested amount is greater than amount owed`() {
        TODO("Not yet implemented")
    }

    @Test
    fun ` on PaymentRequestedEvent, should set state`() {
        TODO("Not yet implemented")
    }

    @Test
    fun ` on PaymentReceivedEvent, should update paid amount`() {
        TODO("Not yet implemented")
    }

    @Test
    fun ` on OrderFullyPaidEvent, should nark aggregate as deleted`() {
        TODO("Not yet implemented")
    }

}
