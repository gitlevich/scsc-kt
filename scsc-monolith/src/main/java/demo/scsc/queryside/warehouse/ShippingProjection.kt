package demo.scsc.queryside.warehouse;

import demo.scsc.Constants;
import demo.scsc.api.warehouse.GetShippingQueryResponse;
import demo.scsc.api.warehouse.ProductAddedToPackageEvent;
import demo.scsc.api.warehouse.ShipmentRequestedEvent;
import demo.scsc.config.JpaPersistenceUnit;
import jakarta.persistence.EntityManager;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.ResetHandler;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.interceptors.MessageHandlerInterceptor;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ProcessingGroup(Constants.PROCESSING_GROUP_WAREHOUSE)
public class ShippingProjection {

    private static final Logger LOG = LoggerFactory.getLogger(ShippingProjection.class);

    public static final String GET_SHIPPING_REQUESTS = "warehouse:getShippingRequests";

    @EventHandler
    public void on(ShipmentRequestedEvent shipmentRequestedEvent, QueryUpdateEmitter queryUpdateEmitter) {

        EntityManager em = JpaPersistenceUnit.forName("SCSC").getNewEntityManager();
        em.getTransaction().begin();
        toEntities(shipmentRequestedEvent).forEach(entity -> {
            em.persist(entity);
            updateSubscribers(
                    entity.getId().getShippingId(),
                    entity.getId().getProductId(),
                    queryUpdateEmitter
            );
        });
        em.getTransaction().commit();
        em.close();

    }

    @EventHandler
    public void on(ProductAddedToPackageEvent productAddedToPackageEvent, QueryUpdateEmitter queryUpdateEmitter) {

        EntityManager em = JpaPersistenceUnit.forName("SCSC").getNewEntityManager();
        em.getTransaction().begin();
        ShippingProductEntity.Id id =
                new ShippingProductEntity.Id(
                        productAddedToPackageEvent.shipmentId(),
                        productAddedToPackageEvent.productId());
        ShippingProductEntity shippingProductEntity = em.find(ShippingProductEntity.class, id);
        em.remove(shippingProductEntity);
        em.getTransaction().commit();
        em.close();

        updateSubscribers(
                productAddedToPackageEvent.shipmentId(),
                productAddedToPackageEvent.productId(),
                true,
                queryUpdateEmitter);
    }

    private void updateSubscribers(UUID shipmentId, UUID productId, QueryUpdateEmitter queryUpdateEmitter) {
        updateSubscribers(shipmentId, productId, false, queryUpdateEmitter);
    }

    private void updateSubscribers(UUID shipmentId, UUID productId, boolean removed, QueryUpdateEmitter queryUpdateEmitter) {
        queryUpdateEmitter.emit(
                subscriptionQueryMessage -> GET_SHIPPING_REQUESTS.equals(subscriptionQueryMessage.getQueryName()),
                new GetShippingQueryResponse.ShippingItem(
                        shipmentId,
                        productId,
                        removed
                )
        );
    }

    @QueryHandler(queryName = GET_SHIPPING_REQUESTS)
    public GetShippingQueryResponse getShippingRequests() {
        EntityManager em = JpaPersistenceUnit.forName("SCSC").getNewEntityManager();
        List<ShippingProductEntity> shippingEntities = em
                .createQuery("SELECT s FROM ShippingProductEntity AS s ORDER BY s.id.shippingId", ShippingProductEntity.class)
                .getResultList();
        GetShippingQueryResponse response = new GetShippingQueryResponse(
                shippingEntities.stream()
                        .map(shippingProductEntity -> new GetShippingQueryResponse.ShippingItem(
                                shippingProductEntity.getId().getShippingId(),
                                shippingProductEntity.getId().getProductId()
                        ))
                        .collect(Collectors.toList())
        );
        em.close();
        return response;
    }


    @ResetHandler
    public void onReset() {
        EntityManager em = JpaPersistenceUnit.forName("SCSC").getNewEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM ShippingProductEntity").executeUpdate();
        em.getTransaction().commit();
    }

    private List<ShippingProductEntity> toEntities(ShipmentRequestedEvent shipmentRequestedEvent) {
        return shipmentRequestedEvent.products().stream()
                .map(productId -> new ShippingProductEntity(new ShippingProductEntity.Id(
                        shipmentRequestedEvent.shipmentId(),
                        productId
                )))
                .collect(Collectors.toList());
    }

    @MessageHandlerInterceptor(messageType = EventMessage.class)
    public void intercept(EventMessage<?> message,
                          InterceptorChain interceptorChain) throws Exception {
        LOG.info("[    EVENT ] " + message.getPayload().toString());
        interceptorChain.proceed();
    }
}
