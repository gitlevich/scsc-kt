package demo.scsc.commandside.warehouse

import demo.scsc.api.warehouse.PackageShippedEvent
import demo.scsc.api.warehouse.RequestShipmentCommand
import demo.scsc.api.warehouse.ShipPackageCommand
import demo.scsc.api.warehouse.ShipmentImpossible
import demo.scsc.api.warehouse.ShipmentRequestedEvent
import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.eventhandling.EventMessage
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.extensions.kotlin.applyEvent
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.MetaData
import org.axonframework.messaging.interceptors.MessageHandlerInterceptor
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.isLive
import org.axonframework.modelling.command.AggregateLifecycle.markDeleted
import org.axonframework.modelling.command.AggregateMember
import org.axonframework.modelling.command.AggregateRoot
import org.slf4j.LoggerFactory
import java.util.*

@AggregateRoot
class Shipment() {
    @AggregateIdentifier
    private lateinit var shipmentId: UUID

    @AggregateMember
    private lateinit var packageContent: PackageContent

    @CommandHandler
    constructor(requestShipmentCommand: RequestShipmentCommand) : this() {
        applyEvent(
            ShipmentRequestedEvent(
                requestShipmentCommand.shipmentId,
                requestShipmentCommand.recipient,
                requestShipmentCommand.products
            ),
            MetaData.with("orderId", requestShipmentCommand.orderId)
        )
    }

    @CommandHandler
    fun on(command: ShipPackageCommand) {
        if (!packageContent.ready())
            throw CommandExecutionException("Package not ready", null, ShipmentImpossible.NOT_READY)
        applyEvent(PackageShippedEvent(command.shipmentId))
    }

    @EventSourcingHandler
    fun on(event: ShipmentRequestedEvent, metaData: MetaData) {
        shipmentId = event.shipmentId
        packageContent = PackageContent((metaData["orderId"] as UUID?)!!, event.products)
    }

    @EventSourcingHandler
    fun on(event: PackageShippedEvent) {
        if (isLive()) {
            /*
                We end this demo here!
                In a real life scenario the process will continue to
                 - contact a delivery company
                 - track deliveries
                 - deal with returns
                 - ...
             */
            LOG.info("THAT'S ALL FOLKS !!!")
        }
        markDeleted()
    }

    @MessageHandlerInterceptor(messageType = CommandMessage::class)
    fun intercept(message: CommandMessage<*>, interceptorChain: InterceptorChain) {
        LOG.info("[  COMMAND ] ${message.payload}")
        interceptorChain.proceed()
    }

    @MessageHandlerInterceptor(messageType = EventMessage::class)
    fun intercept(message: EventMessage<*>, interceptorChain: InterceptorChain) {
        if (isLive()) LOG.info("[    EVENT ] ${message.payload}") else LOG.info("[ SOURCING ] ${message.payload}")
        interceptorChain.proceed()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(Shipment::class.java)
    }
}
