package demo.scsc.api.shoppingcart;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

public record AddProductToCartCommand (
        @TargetAggregateIdentifier UUID cartId,
        String owner,
        UUID productId
) {
}