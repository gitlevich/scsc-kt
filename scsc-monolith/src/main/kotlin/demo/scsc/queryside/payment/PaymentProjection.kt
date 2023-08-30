package demo.scsc.queryside.payment

import com.typesafe.config.Config
import demo.scsc.Constants.PROCESSING_GROUP_PAYMENT
import demo.scsc.api.payment.GetPaymentForOrderQuery
import demo.scsc.api.payment.GetPaymentForOrderQueryResponse
import demo.scsc.api.payment.PaymentReceivedEvent
import demo.scsc.api.payment.PaymentRequestedEvent
import demo.scsc.util.tx
import demo.scsc.util.answer
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.EventMessage
import org.axonframework.eventhandling.ResetHandler
import org.axonframework.eventhandling.replay.ResetContext
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.interceptors.MessageHandlerInterceptor
import org.axonframework.queryhandling.QueryHandler
import org.slf4j.LoggerFactory
import java.math.BigDecimal

@ProcessingGroup(PROCESSING_GROUP_PAYMENT)
class PaymentProjection(private val appConfig: Config) {

    @EventHandler
    fun on(event: PaymentRequestedEvent) {
        tx(appConfig) { it.persist(event.toEntity()) }
    }

    @EventHandler
    fun on(event: PaymentReceivedEvent) {
        tx(appConfig) {
            it.find(Payment::class.java, event.orderPaymentId)?.let { payment ->
                it.merge(payment.withPaidAmount(event.amount))
            }
        }
    }

    @QueryHandler
    fun on(query: GetPaymentForOrderQuery): GetPaymentForOrderQueryResponse =
        answer(query, appConfig) {
            val payment = it
                .createQuery("SELECT p FROM Payment p WHERE p.orderId = :orderId", Payment::class.java)
                .setParameter("orderId", query.orderId)
                .singleResult

            GetPaymentForOrderQueryResponse(
                id = payment.id,
                orderId = payment.orderId,
                requestedAmount = payment.requestedAmount,
                paidAmount = payment.paidAmount
            )
        }

    @ResetHandler
    fun onReset(resetContext: ResetContext<*>) {
        tx(appConfig) { it.createQuery("DELETE FROM Payment").executeUpdate() }
    }

    @MessageHandlerInterceptor(messageType = EventMessage::class)
    fun intercept(message: EventMessage<*>, interceptorChain: InterceptorChain) {
        LOG.info("[    EVENT ] ${message.payload}")
        interceptorChain.proceed()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(PaymentProjection::class.java)

        private fun PaymentRequestedEvent.toEntity() = Payment(
            id = orderPaymentId,
            orderId = orderId,
            requestedAmount = amount,
            paidAmount = BigDecimal.ZERO
        )
    }
}
