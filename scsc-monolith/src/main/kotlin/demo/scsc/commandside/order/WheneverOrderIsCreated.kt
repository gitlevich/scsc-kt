package demo.scsc.commandside.order

import demo.scsc.api.order
import demo.scsc.infra.EmailService
import jakarta.mail.MessagingException
import org.axonframework.eventhandling.DisallowReplay
import org.axonframework.eventhandling.EventHandler
import org.slf4j.LoggerFactory

class WheneverOrderIsCreated(private val emailService: EmailService) {

    @DisallowReplay
    @EventHandler
    fun on(event: order.OrderCreatedEvent) {
        log.debug("[   POLICY ] Whenever order is created, send an email to the owner (orderId={})", event.orderId)
        try {
            emailService.sendEmail(
                event.owner,
                "New order ${event.orderId}",
                "Thank you for your order!\n\n${event.items}"
            )
        } catch (e: MessagingException) {
            log.error("Failed to send email", e)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(WheneverOrderIsCreated::class.java)
    }
}
