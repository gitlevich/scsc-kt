package demo.scsc.queryside.payment

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
class PaymentProjection {

    @EventHandler
    fun on(event: PaymentRequestedEvent) {
        tx { it.persist(event.toEntity()) }
    }

    @EventHandler
    fun on(event: PaymentReceivedEvent) {
        tx { em ->
            em.find(PaymentEntity::class.java, event.orderPaymentId)?.let { paymentEntity ->
                var paid = paymentEntity.paidAmount!!
                paid = paid.add(event.amount)
                paymentEntity.paidAmount = paid
                em.merge(paymentEntity)
            }

        }
    }

    @QueryHandler
    fun on(query: GetPaymentForOrderQuery): GetPaymentForOrderQueryResponse =
        answer(query) {
            val paymentEntity = it
                .createQuery("SELECT p FROM PaymentEntity p WHERE p.orderId = :orderId", PaymentEntity::class.java)
                .setParameter("orderId", query.orderId)
                .singleResult

            GetPaymentForOrderQueryResponse(
                paymentEntity.id!!,
                paymentEntity.orderId!!,
                paymentEntity.requestedAmount!!,
                paymentEntity.paidAmount!!
            )
        }

    @ResetHandler
    fun onReset(resetContext: ResetContext<*>) {
        tx { it.createQuery("DELETE FROM PaymentEntity").executeUpdate() }
    }

    @MessageHandlerInterceptor(messageType = EventMessage::class)
    fun intercept(message: EventMessage<*>, interceptorChain: InterceptorChain) {
        LOG.info("[    EVENT ] ${message.payload}")
        interceptorChain.proceed()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(PaymentProjection::class.java)

        private fun PaymentRequestedEvent.toEntity() = PaymentEntity().apply {
            id = orderPaymentId
            orderId = this@toEntity.orderId
            requestedAmount = amount
            paidAmount = BigDecimal.ZERO
        }
    }
}
