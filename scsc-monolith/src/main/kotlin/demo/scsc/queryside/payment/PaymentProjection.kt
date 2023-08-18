package demo.scsc.queryside.payment

import demo.scsc.api.payment.GetPaymentForOrderQuery
import demo.scsc.api.payment.GetPaymentForOrderQueryResponse
import demo.scsc.api.payment.PaymentReceivedEvent
import demo.scsc.api.payment.PaymentRequestedEvent
import demo.scsc.config.JpaPersistenceUnit.Companion.forName
import jakarta.persistence.NoResultException
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.EventMessage
import org.axonframework.eventhandling.ResetHandler
import org.axonframework.eventhandling.replay.ResetContext
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.interceptors.MessageHandlerInterceptor
import org.axonframework.queryhandling.QueryExecutionException
import org.axonframework.queryhandling.QueryHandler
import org.slf4j.LoggerFactory
import java.math.BigDecimal

@ProcessingGroup("payment")
class PaymentProjection {
    @EventHandler
    fun on(paymentRequestedEvent: PaymentRequestedEvent) {
        val em = forName("SCSC")!!.newEntityManager
        em.transaction.begin()
        em.persist(toEntity(paymentRequestedEvent))
        em.transaction.commit()
        em.close()
    }

    @EventHandler
    fun on(paymentReceivedEvent: PaymentReceivedEvent) {
        val em = forName("SCSC")!!.newEntityManager
        em.transaction.begin()
        val paymentEntity = em.find(PaymentEntity::class.java, paymentReceivedEvent.orderPaymentId)
        var paid = paymentEntity.paidAmount
        paid = paid!!.add(paymentReceivedEvent.amount)
        paymentEntity.paidAmount = paid
        em.merge(paymentEntity)
        em.transaction.commit()
        em.close()
    }

    @QueryHandler
    fun on(getPaymentForOrderQuery: GetPaymentForOrderQuery): GetPaymentForOrderQueryResponse {
        val em = forName("SCSC")!!.newEntityManager
        return try {
            val paymentEntity =
                em.createQuery("SELECT p FROM PaymentEntity p WHERE p.orderId = :orderId", PaymentEntity::class.java)
                    .setParameter("orderId", getPaymentForOrderQuery.orderId)
                    .singleResult
            GetPaymentForOrderQueryResponse(
                paymentEntity.id!!,
                paymentEntity.orderId!!,
                paymentEntity.requestedAmount!!,
                paymentEntity.paidAmount!!
            )
        } catch (e: NoResultException) {
            throw QueryExecutionException("No payment process for order " + getPaymentForOrderQuery.orderId, null)
        }
    }

    @ResetHandler
    fun onReset(resetContext: ResetContext<*>?) {
        val em = forName("SCSC")!!.newEntityManager
        em.transaction.begin()
        em.createQuery("DELETE FROM PaymentEntity").executeUpdate()
        em.transaction.commit()
    }

    private fun toEntity(paymentRequestedEvent: PaymentRequestedEvent): PaymentEntity {
        val paymentEntity = PaymentEntity()
        paymentEntity.id = paymentRequestedEvent.orderPaymentId
        paymentEntity.orderId = paymentRequestedEvent.orderId
        paymentEntity.requestedAmount = paymentRequestedEvent.amount
        paymentEntity.paidAmount = BigDecimal.ZERO
        return paymentEntity
    }

    @MessageHandlerInterceptor(messageType = EventMessage::class)
    fun intercept(
        message: EventMessage<*>,
        interceptorChain: InterceptorChain
    ) {
        LOG.info("[    EVENT ] " + message.payload.toString())
        interceptorChain.proceed()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(PaymentProjection::class.java)
    }
}
