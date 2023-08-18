package demo.scsc.api.shoppingcart;

import java.util.UUID;

public record ProductRemovedFromCartEvent(
        UUID cartId, UUID productId
) {
}
