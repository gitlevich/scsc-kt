package demo.scsc.commandside.warehouse

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
import java.util.function.Function
import java.util.stream.Collectors

class PackageContent(private val orderId: UUID, items: List<UUID>) {
    private val items: MutableMap<UUID, Boolean>

    init {
        this.items = items.stream().collect(Collectors.toMap(
            { item: UUID? -> item }, { false })
        )
    }

    fun ready(): Boolean {
        return !items.containsValue(false)
    }

    @CommandHandler
    fun on(addProductToPackageCommand: AddProductToPackageCommand) {

        if (!items.containsKey(addProductToPackageCommand.productId)) {
            throw CommandExecutionException("product not part of this shipment", null)
        }
        if (items[addProductToPackageCommand.productId]!!) {
            throw CommandExecutionException("product already added to package", null)
        }

        applyEvent(
            ProductAddedToPackageEvent(
                addProductToPackageCommand.shipmentId,
                addProductToPackageCommand.productId
            )
        )
    }

    @EventSourcingHandler
    fun on(productAddedToPackageEvent: ProductAddedToPackageEvent) {
        items[productAddedToPackageEvent.productId] = true
        if (AggregateLifecycle.isLive() && ready()) {
            applyEvent(
                PackageReadyEvent(
                    productAddedToPackageEvent.shipmentId,
                    orderId
                )
            )
        }
    }

    @MessageHandlerInterceptor(messageType = CommandMessage::class)
    fun intercept(
        message: CommandMessage<*>,
        interceptorChain: InterceptorChain
    ) {
        LOG.info("[  COMMAND ] " + message.payload.toString())
        interceptorChain.proceed()
    }

    @MessageHandlerInterceptor(messageType = EventMessage::class)
    fun intercept(
        message: EventMessage<*>,
        interceptorChain: InterceptorChain
    ) {
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
