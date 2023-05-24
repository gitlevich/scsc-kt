package demo.scsc.api.warehouse;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

public record AddProductToPackageCommand (
    @TargetAggregateIdentifier UUID shipmentId,
     UUID productId
) {
}
