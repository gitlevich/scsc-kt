package demo.scsc.commandside.warehouse;

import demo.scsc.api.warehouse.AddProductToPackageCommand;
import demo.scsc.api.warehouse.PackageReadyEvent;
import demo.scsc.api.warehouse.ProductAddedToPackageEvent;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.interceptors.MessageHandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;
import static org.axonframework.modelling.command.AggregateLifecycle.isLive;

public class PackageContent {

    public static final Logger LOG = LoggerFactory.getLogger(PackageContent.class);

    private final Map<UUID, Boolean> items;

    private final UUID orderId;

    public PackageContent(UUID orderId, List<UUID> items) {
        this.orderId = orderId;
        this.items = items.stream().collect(Collectors.toMap(item -> item, item -> false));
    }

    public boolean ready() {
        return !items.containsValue(false);
    }

    @CommandHandler
    public void on(AddProductToPackageCommand addProductToPackageCommand) {

        // validation

        if (!items.containsKey(addProductToPackageCommand.productId())) {
            throw new CommandExecutionException("product not part of this shipment", null);
        }

        if (items.get(addProductToPackageCommand.productId())) {
            throw new CommandExecutionException("product already added to package", null);
        }

        // applying event(s)

        apply(new ProductAddedToPackageEvent(
                addProductToPackageCommand.shipmentId(),
                addProductToPackageCommand.productId()
        ));

    }

    @EventSourcingHandler
    public void on(ProductAddedToPackageEvent productAddedToPackageEvent) {
        items.put(productAddedToPackageEvent.productId(), true);

        if (isLive() && ready()) {
            apply(new PackageReadyEvent(
                    productAddedToPackageEvent.shipmentId(),
                    orderId
            ));
        }
    }

    @MessageHandlerInterceptor(messageType = CommandMessage.class)
    public void intercept(CommandMessage<?> message,
                          InterceptorChain interceptorChain) throws Exception {
        LOG.info("[  COMMAND ] " + message.getPayload().toString());
        interceptorChain.proceed();
    }

    @MessageHandlerInterceptor(messageType = EventMessage.class)
    public void intercept(EventMessage<?> message,
                          InterceptorChain interceptorChain) throws Exception {
        if (isLive()) {
            LOG.info("[    EVENT ] " + message.getPayload().toString());
        } else {
            LOG.info("[ SOURCING ] " + message.getPayload().toString());
        }
        interceptorChain.proceed();
    }

}
