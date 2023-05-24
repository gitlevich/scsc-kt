package demo.scsc.api.shoppingcart;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

public record AbandonCartCommand (
    @TargetAggregateIdentifier UUID cartId
){
}
