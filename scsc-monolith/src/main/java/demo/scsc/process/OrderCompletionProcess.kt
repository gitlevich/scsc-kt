package demo.scsc.process;

import demo.scsc.api.order.CompleteOrderCommand;
import demo.scsc.api.order.OrderCreatedEvent;
import demo.scsc.api.payment.OrderFullyPaidEvent;
import demo.scsc.api.payment.RequestPaymentCommand;
import demo.scsc.api.warehouse.PackageReadyEvent;
import demo.scsc.api.warehouse.PackageShippedEvent;
import demo.scsc.api.warehouse.RequestShipmentCommand;
import demo.scsc.api.warehouse.ShipPackageCommand;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.interceptors.MessageHandlerInterceptor;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Collectors;


public class OrderCompletionProcess {

    public static final Logger LOG = LoggerFactory.getLogger(OrderCompletionProcess.class);

    private UUID orderId;
    private UUID orderPaymentId;
    private UUID shipmentId;
    private boolean paid;
    private boolean shipmentReady;


    @StartSaga
    @SagaEventHandler(keyName = "orderId", associationProperty = "orderId")
    public void on(OrderCreatedEvent orderCreatedEvent, CommandGateway commandGateway) {

        orderId = orderCreatedEvent.orderId();
        orderPaymentId = UUID.randomUUID();
        shipmentId = UUID.randomUUID();

        SagaLifecycle.associateWith("orderPaymentId", orderPaymentId.toString());
        SagaLifecycle.associateWith("shipmentId", shipmentId.toString());

        commandGateway.send(new RequestPaymentCommand(
                orderPaymentId,
                orderCreatedEvent.orderId(),
                orderCreatedEvent.items().stream()
                        .map(OrderCreatedEvent.OrderItem::price)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        ), errorHandlingCallback());


        commandGateway.send(new RequestShipmentCommand(
                shipmentId,
                orderCreatedEvent.orderId(),
                orderCreatedEvent.owner(),
                orderCreatedEvent.items().stream()
                        .map(OrderCreatedEvent.OrderItem::id)
                        .collect(Collectors.toList())
        ), errorHandlingCallback());
    }

    @SagaEventHandler(keyName = "orderId", associationProperty = "orderId")
    public void on(OrderFullyPaidEvent orderFullyPaidEvent, CommandGateway commandGateway) {

        paid = true;

        if (shipmentReady) {
            commandGateway.send(new ShipPackageCommand(shipmentId));
        }

    }

    @SagaEventHandler(keyName = "shipmentId", associationProperty = "shipmentId")
    public void on(PackageReadyEvent packageReadyEvent, CommandGateway commandGateway) {

        shipmentReady = true;

        if (paid) {
            commandGateway.send(new ShipPackageCommand(shipmentId));
        }
    }

    @SagaEventHandler(keyName = "shipmentId", associationProperty = "shipmentId")
    @EndSaga
    public void on(PackageShippedEvent packageShippedEvent, CommandGateway commandGateway) {
        commandGateway.send(new CompleteOrderCommand(orderId));
    }


    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }


    @SuppressWarnings("unused")
    public UUID getOrderPaymentId() {
        return orderPaymentId;
    }

    @SuppressWarnings("unused")
    public void setOrderPaymentId(UUID orderPaymentId) {
        this.orderPaymentId = orderPaymentId;
    }

    @SuppressWarnings("unused")
    public UUID getShipmentId() {
        return shipmentId;
    }

    @SuppressWarnings("unused")
    public void setShipmentId(UUID shipmentId) {
        this.shipmentId = shipmentId;
    }

    @SuppressWarnings("unused")
    public boolean isPaid() {
        return paid;
    }

    @SuppressWarnings("unused")
    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    @SuppressWarnings("unused")
    public boolean isShipmentReady() {
        return shipmentReady;
    }

    @SuppressWarnings("unused")
    public void setShipmentReady(boolean shipmentReady) {
        this.shipmentReady = shipmentReady;
    }

    @NotNull
    private static <T> CommandCallback<T, Object> errorHandlingCallback() {
        return (command, response) -> {
            if (response.isExceptional()) {
            /*
                options to handle command processing errors
                 - schedule a retry
                 - send another command
                 - publish an event
             */
                response.exceptionResult().printStackTrace();
            }
        };
    }

    @MessageHandlerInterceptor(messageType = EventMessage.class)
    public void intercept(EventMessage<?> message,
                          InterceptorChain interceptorChain) throws Exception {
        LOG.info("[    EVENT ] " + message.getPayload().toString());
        interceptorChain.proceed();
    }
}
