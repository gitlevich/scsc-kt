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
import org.axonframework.modelling.saga.SagaLifecycle.associateWith
import org.axonframework.modelling.saga.StartSaga
import org.slf4j.LoggerFactory
import java.util.*

@Suppress("UNUSED_PARAMETER")
class OrderCompletionProcess {
    lateinit var orderId: UUID

    private lateinit var orderPaymentId: UUID

    private lateinit var shipmentId: UUID

    private var isPaid = false

    private var isShipmentReady = false

    @StartSaga
    @SagaEventHandler(keyName = "orderId", associationProperty = "orderId")
    fun on(event: OrderCreatedEvent, commandGateway: CommandGateway, nextUuid: () -> UUID = { UUID.randomUUID() }) {
        orderId = event.orderId
        orderPaymentId = nextUuid()
        shipmentId = nextUuid()
        associateWith("orderPaymentId", orderPaymentId.toString())
        associateWith("shipmentId", shipmentId.toString())
        commandGateway.send(
            RequestPaymentCommand(
                orderPaymentId = orderPaymentId,
                orderId = event.orderId,
                amount = event.items.asSequence()
                    .map { it.price }
                    .reduce { acc, c -> acc.add(c) }
            ),
            errorHandlingCallback()
        )
        commandGateway.send(
            RequestShipmentCommand(
                shipmentId = shipmentId,
                orderId = event.orderId,
                recipient = event.owner,
                products = event.items.asSequence()
                    .map { it.id }
                    .toList()
            ),
            errorHandlingCallback()
        )
    }

    @SagaEventHandler(keyName = "orderId", associationProperty = "orderId")
    fun on(event: OrderFullyPaidEvent, commandGateway: CommandGateway) {
        isPaid = true
        if (isShipmentReady) commandGateway.send<Any>(ShipPackageCommand(shipmentId))
    }

    @SagaEventHandler(keyName = "shipmentId", associationProperty = "shipmentId")
    fun on(event: PackageReadyEvent, commandGateway: CommandGateway) {
        isShipmentReady = true
        if (isPaid) commandGateway.send<Any>(ShipPackageCommand(shipmentId))
    }

    @SagaEventHandler(keyName = "shipmentId", associationProperty = "shipmentId")
    @EndSaga
    fun on(event: PackageShippedEvent, commandGateway: CommandGateway) {
        commandGateway.send<Any>(CompleteOrderCommand(orderId))
    }

    @MessageHandlerInterceptor(messageType = EventMessage::class)
    fun intercept(message: EventMessage<*>, interceptorChain: InterceptorChain) {
        LOG.info("[    EVENT ] " + message.payload.toString())
        interceptorChain.proceed()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(OrderCompletionProcess::class.java)
        private fun <T> errorHandlingCallback(): CommandCallback<T, Any> =
            CommandCallback { _: CommandMessage<out T>?, response: CommandResultMessage<*> ->
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
