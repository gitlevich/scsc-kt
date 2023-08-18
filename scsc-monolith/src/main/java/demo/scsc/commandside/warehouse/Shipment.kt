package demo.scsc.commandside.warehouse;

import demo.scsc.api.warehouse.*;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.MetaData;
import org.axonframework.messaging.interceptors.MessageHandlerInterceptor;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.*;

public class Shipment {

    public static final Logger LOG = LoggerFactory.getLogger(Shipment.class);

    @AggregateIdentifier
    private UUID shipmentId;

    @AggregateMember
    private PackageContent packageContent;

    @SuppressWarnings("unused")
    public Shipment() {
        // required no-args constructor
    }

    @CommandHandler
    public Shipment(RequestShipmentCommand requestShipmentCommand) {

       /* -------------------------
               validation
       ------------------------- */

        if (requestShipmentCommand.orderId() == null) {
            throw new CommandExecutionException("Can't create shipment without order", null);
        }

        if (requestShipmentCommand.products() == null || requestShipmentCommand.products().isEmpty()) {
            throw new CommandExecutionException("Nothing to ship", null);
        }

        /* -------------------------
                notification
        ------------------------- */


        apply(
                new ShipmentRequestedEvent(
                        requestShipmentCommand.shipmentId(),
                        requestShipmentCommand.recipient(),
                        requestShipmentCommand.products()
                ),
                MetaData.with("orderId", requestShipmentCommand.orderId())
        );

    }


    @CommandHandler
    public void on(ShipPackageCommand shipPackageCommand) {

        /* -------------------------
                validation
        ------------------------- */

        if (!packageContent.ready()) {
            throw new CommandExecutionException("Package not ready", null, ShipmentImpossible.NOT_READY);
        }

        /* -------------------------
                notification
        ------------------------- */

        apply(new PackageShippedEvent(shipmentId));
    }


    @EventSourcingHandler
    public void on(ShipmentRequestedEvent shipmentRequestedEvent, MetaData metaData) {
        this.shipmentId = shipmentRequestedEvent.shipmentId();
        this.packageContent = new PackageContent((UUID) metaData.get("orderId"), shipmentRequestedEvent.products());
    }

    @EventSourcingHandler
    public void on(PackageShippedEvent packageShippedEvent) {
        if (isLive()) {
            /*
                We end this demo here!
                In a real life scenario the process will continue to
                 - contact a delivery company
                 - track deliveries
                 - deal with returns
                 - ...
             */
            LOG.info("THAT'S ALL FOLKS !!!");
        }
        markDeleted();
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
