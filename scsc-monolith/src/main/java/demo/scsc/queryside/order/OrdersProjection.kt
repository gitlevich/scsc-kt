package demo.scsc.queryside.order;

import demo.scsc.Constants;
import demo.scsc.api.order.GetOrdersQuery;
import demo.scsc.api.order.GetOrdersQueryResponse;
import demo.scsc.api.order.OrderCompletedEvent;
import demo.scsc.api.order.OrderCreatedEvent;
import demo.scsc.api.payment.OrderFullyPaidEvent;
import demo.scsc.api.warehouse.PackageReadyEvent;
import demo.scsc.config.JpaPersistenceUnit;
import jakarta.persistence.EntityManager;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.ResetHandler;
import org.axonframework.eventhandling.replay.ResetContext;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.interceptors.MessageHandlerInterceptor;
import org.axonframework.queryhandling.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@ProcessingGroup(Constants.PROCESSING_GROUP_ORDER)
public class OrdersProjection {

    private static final Logger LOG = LoggerFactory.getLogger(OrdersProjection.class);

    @EventHandler
    public void on(OrderCreatedEvent orderCreatedEvent) {
        EntityManager em = JpaPersistenceUnit.forName("SCSC").getNewEntityManager();
        em.getTransaction().begin();
        em.persist(toEntity(orderCreatedEvent));
        em.getTransaction().commit();
        em.close();
    }

    @EventHandler
    public void on(OrderFullyPaidEvent orderFullyPaidEvent) {
        EntityManager em = JpaPersistenceUnit.forName("SCSC").getNewEntityManager();
        em.getTransaction().begin();
        OrderEntity orderEntity = em.find(OrderEntity.class, orderFullyPaidEvent.orderId());
        orderEntity.setPaid(true);
        em.merge(orderEntity);
        em.getTransaction().commit();
        em.close();
    }

    @EventHandler
    public void on(PackageReadyEvent packageReadyEvent) {
        EntityManager em = JpaPersistenceUnit.forName("SCSC").getNewEntityManager();
        em.getTransaction().begin();
        OrderEntity orderEntity = em.find(OrderEntity.class, packageReadyEvent.orderId());
        orderEntity.setPrepared(true);
        em.merge(orderEntity);
        em.getTransaction().commit();
        em.close();
    }


    @EventHandler
    public void on(OrderCompletedEvent orderCompletedEvent) {
        EntityManager em = JpaPersistenceUnit.forName("SCSC").getNewEntityManager();
        em.getTransaction().begin();
        OrderEntity orderEntity = em.find(OrderEntity.class, orderCompletedEvent.orderId());
        orderEntity.setReady(true);
        em.merge(orderEntity);
        em.getTransaction().commit();
        em.close();
    }


    @QueryHandler
    public GetOrdersQueryResponse getOrders(GetOrdersQuery query) {
        EntityManager em = JpaPersistenceUnit.forName("SCSC").getNewEntityManager();
        List<OrderEntity> orderEntities = em
                .createQuery("SELECT p FROM OrderEntity AS p WHERE owner = ?1", OrderEntity.class)
                .setParameter(1, query.owner())
                .getResultList();
        GetOrdersQueryResponse response = new GetOrdersQueryResponse(
                orderEntities.stream()
                        .map(orderEntity -> new GetOrdersQueryResponse.Order(
                                orderEntity.getId(),
                                orderEntity.getItems().stream().
                                        map(OrderEntityItem::getPrice)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                                orderEntity.getItems().stream()
                                        .map(item -> new GetOrdersQueryResponse.OrderLine(
                                                item.getName(),
                                                item.getPrice()
                                        )).collect(Collectors.toList()),
                                orderEntity.getOwner(),
                                orderEntity.isPaid(),
                                orderEntity.isPrepared(),
                                orderEntity.isReady())
                        )
                        .collect(Collectors.toList())
        );
        em.close();
        return response;
    }

    @ResetHandler
    public void onReset(ResetContext<?> resetContext) {
        EntityManager em = JpaPersistenceUnit.forName("SCSC").getNewEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM OrderEntity").executeUpdate();
        em.getTransaction().commit();
    }

    private OrderEntity toEntity(OrderCreatedEvent orderCreatedEvent) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(orderCreatedEvent.orderId());
        orderEntity.setOwner(orderCreatedEvent.owner());
        List<OrderEntityItem> items = orderCreatedEvent.items().stream().map(item -> {
            OrderEntityItem entity = new OrderEntityItem();
            entity.setId(item.id());
            entity.setName(item.name());
            entity.setPrice(item.price());
            return entity;
        }).collect(Collectors.toList());
        orderEntity.setItems(items);
        return orderEntity;
    }


    @MessageHandlerInterceptor(messageType = EventMessage.class)
    public void intercept(EventMessage<?> message,
                          InterceptorChain interceptorChain) throws Exception {
        LOG.info("[    EVENT ] " + message.getPayload().toString());
        interceptorChain.proceed();
    }

}
