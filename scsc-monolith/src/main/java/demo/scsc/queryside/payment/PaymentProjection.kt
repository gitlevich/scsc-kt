package demo.scsc.queryside.payment;

import demo.scsc.api.payment.GetPaymentForOrderQuery;
import demo.scsc.api.payment.GetPaymentForOrderQueryResponse;
import demo.scsc.api.payment.PaymentReceivedEvent;
import demo.scsc.api.payment.PaymentRequestedEvent;
import demo.scsc.config.JpaPersistenceUnit;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.ResetHandler;
import org.axonframework.eventhandling.replay.ResetContext;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.interceptors.MessageHandlerInterceptor;
import org.axonframework.queryhandling.QueryExecutionException;
import org.axonframework.queryhandling.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

@ProcessingGroup("payment")
public class PaymentProjection {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentProjection.class);

    @EventHandler
    public void on(PaymentRequestedEvent paymentRequestedEvent) {
        EntityManager em = JpaPersistenceUnit.forName("SCSC").getNewEntityManager();
        em.getTransaction().begin();
        em.persist(toEntity(paymentRequestedEvent));
        em.getTransaction().commit();
        em.close();
    }

    @EventHandler
    public void on(PaymentReceivedEvent paymentReceivedEvent) {
        EntityManager em = JpaPersistenceUnit.forName("SCSC").getNewEntityManager();
        em.getTransaction().begin();
        PaymentEntity paymentEntity = em.find(PaymentEntity.class, paymentReceivedEvent.orderPaymentId());
        BigDecimal paid = paymentEntity.getPaidAmount();
        paid = paid.add(paymentReceivedEvent.amount());
        paymentEntity.setPaidAmount(paid);
        em.merge(paymentEntity);
        em.getTransaction().commit();
        em.close();
    }


    @QueryHandler
    public GetPaymentForOrderQueryResponse on(GetPaymentForOrderQuery getPaymentForOrderQuery) {
        EntityManager em = JpaPersistenceUnit.forName("SCSC").getNewEntityManager();
        try {
            PaymentEntity paymentEntity =
                    em.createQuery("SELECT p FROM PaymentEntity p WHERE p.orderId = :orderId", PaymentEntity.class)
                            .setParameter("orderId", getPaymentForOrderQuery.orderId())
                            .getSingleResult();
            return new GetPaymentForOrderQueryResponse(
                    paymentEntity.getId(),
                    paymentEntity.getOrderId(),
                    paymentEntity.getRequestedAmount(),
                    paymentEntity.getPaidAmount()
            );
        } catch (NoResultException e) {
            throw new QueryExecutionException("No payment process for order " + getPaymentForOrderQuery.orderId(), null);
        }
    }


    @ResetHandler
    public void onReset(ResetContext<?> resetContext) {
        EntityManager em = JpaPersistenceUnit.forName("SCSC").getNewEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM PaymentEntity").executeUpdate();
        em.getTransaction().commit();
    }


    private PaymentEntity toEntity(PaymentRequestedEvent paymentRequestedEvent) {
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setId(paymentRequestedEvent.orderPaymentId());
        paymentEntity.setOrderId(paymentRequestedEvent.orderId());
        paymentEntity.setRequestedAmount(paymentRequestedEvent.amount());
        paymentEntity.setPaidAmount(BigDecimal.ZERO);
        return paymentEntity;
    }

    @MessageHandlerInterceptor(messageType = EventMessage.class)
    public void intercept(EventMessage<?> message,
                          InterceptorChain interceptorChain) throws Exception {
        LOG.info("[    EVENT ] " + message.getPayload().toString());
        interceptorChain.proceed();
    }
}
