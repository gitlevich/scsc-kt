package demo.scsc.commandside.shipment

import demo.scsc.api.warehouse.AddProductToPackageCommand
import demo.scsc.api.warehouse.PackageReadyEvent
import demo.scsc.api.warehouse.ProductAddedToPackageEvent
import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.eventhandling.EventMessage
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.extensions.kotlin.applyEvent
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.interceptors.MessageHandlerInterceptor
import org.axonframework.modelling.command.AggregateLifecycle
import org.slf4j.LoggerFactory
import java.util.*

class PackageContent(private val orderId: UUID, items: List<UUID>) {
    private val items: MutableMap<UUID, Boolean> = items.associateWith { false }.toMutableMap()

    fun ready(): Boolean = !items.containsValue(false)

    @CommandHandler
    fun on(command: AddProductToPackageCommand) {
        if (!items.containsKey(command.productId)) {
            throw CommandExecutionException("product not part of this shipment", null)
        }
        if (items[command.productId]!!) {
            throw CommandExecutionException("product already added to package", null)
        }

        applyEvent(
            ProductAddedToPackageEvent(
                command.shipmentId,
                command.productId
            )
        )
    }

    @EventSourcingHandler
    fun on(event: ProductAddedToPackageEvent) {
        items[event.productId] = true
        if (AggregateLifecycle.isLive() && ready()) {
            applyEvent(
                PackageReadyEvent(
                    event.shipmentId,
                    orderId
                )
            )
        }
    }

    @MessageHandlerInterceptor(messageType = CommandMessage::class)
    fun intercept(message: CommandMessage<*>, interceptorChain: InterceptorChain) {
        LOG.info("[  COMMAND ] " + message.payload.toString())
        interceptorChain.proceed()
    }

    @MessageHandlerInterceptor(messageType = EventMessage::class)
    fun intercept(message: EventMessage<*>, interceptorChain: InterceptorChain) {
        if (AggregateLifecycle.isLive()) {
            LOG.info("[    EVENT ] " + message.payload.toString())
        } else {
            LOG.info("[ SOURCING ] " + message.payload.toString())
        }
        interceptorChain.proceed()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(PackageContent::class.java)
    }
}
