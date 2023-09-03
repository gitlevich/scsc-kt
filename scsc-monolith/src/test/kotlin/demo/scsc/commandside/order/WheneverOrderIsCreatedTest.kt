package demo.scsc.commandside.order

import demo.scsc.Order
import demo.scsc.infra.EmailService
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class WheneverOrderIsCreatedTest {
    private val emailService = mockk<EmailService>(relaxed = true)
    private val policy = WheneverOrderIsCreated(emailService)

    @Test
    fun `should send CreateOrderCommand and CompleteCartCheckoutCommand on CartCheckoutRequestedEvent`() {
        policy.on(Order.orderCreatedEvent)

        verify { emailService.sendEmail(any(), any(), any()) }
    }
}
