package demo.scsc.api.order;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

public record CompleteOrderCommand(
        @TargetAggregateIdentifier UUID orderId
) {
}