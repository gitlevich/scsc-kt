package demo.scsc.api.warehouse;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

public record ShipPackageCommand(
        @TargetAggregateIdentifier UUID shipmentId
) {
}
