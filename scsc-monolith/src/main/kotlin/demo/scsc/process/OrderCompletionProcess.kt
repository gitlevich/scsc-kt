package demo.scsc.process

import demo.scsc.api.order.CompleteOrderCommand
import demo.scsc.api.order.OrderCreatedEvent
import demo.scsc.api.payment.OrderFullyPaidEvent
import demo.scsc.api.payment.RequestPaymentCommand
import demo.scsc.api.warehouse.PackageReadyEvent
import demo.scsc.api.warehouse.PackageShippedEvent
import demo.scsc.api.warehouse.RequestShipmentCommand
import demo.scsc.api.warehouse.ShipPackageCommand
import org.axonframework.commandhandling.CommandCallback
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.commandhandling.CommandResultMessage
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.EventMessage
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.interceptors.MessageHandlerInterceptor
import org.axonframework.modelling.saga.EndSaga
import org.axonframework.modelling.saga.SagaEventHandler
import org.axonframework.modelling.saga.SagaLifecycle.*
import org.axonframework.modelling.saga.StartSaga
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.util.*
import java.util.stream.Collectors

class OrderCompletionProcess {
    var orderId: UUID? = null

    lateinit var orderPaymentId: UUID

    lateinit var shipmentId: UUID

    var isPaid = false

    var isShipmentReady = false

    @StartSaga
    @SagaEventHandler(keyName = "orderId", associationProperty = "orderId")
    fun on(orderCreatedEvent: OrderCreatedEvent, commandGateway: CommandGateway) {
        orderId = orderCreatedEvent.orderId
        orderPaymentId = UUID.randomUUID()
        shipmentId = UUID.randomUUID()
        associateWith("orderPaymentId", orderPaymentId.toString())
        associateWith("shipmentId", shipmentId.toString())
        commandGateway.send(
            RequestPaymentCommand(
                orderPaymentId,
                orderCreatedEvent.orderId,
                orderCreatedEvent.items.stream()
                    .map(OrderCreatedEvent.OrderItem::price)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
            ), errorHandlingCallback()
        )
        commandGateway.send(
            RequestShipmentCommand(
                shipmentId,
                orderCreatedEvent.orderId,
                orderCreatedEvent.owner,
                orderCreatedEvent.items.stream()
                    .map(OrderCreatedEvent.OrderItem::id)
                    .collect(Collectors.toList())
            ), errorHandlingCallback()
        )
    }

    @SagaEventHandler(keyName = "orderId", associationProperty = "orderId")
    fun on(orderFullyPaidEvent: OrderFullyPaidEvent?, commandGateway: CommandGateway) {
        isPaid = true
        if (isShipmentReady) {
            commandGateway.send<Any>(ShipPackageCommand(shipmentId))
        }
    }

    @SagaEventHandler(keyName = "shipmentId", associationProperty = "shipmentId")
    fun on(packageReadyEvent: PackageReadyEvent?, commandGateway: CommandGateway) {
        isShipmentReady = true
        if (isPaid) {
            commandGateway.send<Any>(ShipPackageCommand(shipmentId))
        }
    }

    @SagaEventHandler(keyName = "shipmentId", associationProperty = "shipmentId")
    @EndSaga
    fun on(packageShippedEvent: PackageShippedEvent, commandGateway: CommandGateway) {
        commandGateway.send<Any>(CompleteOrderCommand(orderId!!))
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
        private val LOG = LoggerFactory.getLogger(OrderCompletionProcess::class.java)
        private fun <T> errorHandlingCallback(): CommandCallback<T, Any> {
            return CommandCallback { command: CommandMessage<out T>?, response: CommandResultMessage<*> ->
                if (response.isExceptional) {
                    /*
                options to handle command processing errors
                 - schedule a retry
                 - send another command
                 - publish an event
             */
                    response.exceptionResult().printStackTrace()
                }
            }
        }
    }
}
