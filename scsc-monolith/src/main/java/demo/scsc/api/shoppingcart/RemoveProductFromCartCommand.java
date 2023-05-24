package demo.scsc.api.shoppingcart;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

public record RemoveProductFromCartCommand(
        @TargetAggregateIdentifier UUID cartId,
        UUID productId
) {
}
