package demo.scsc.api.shoppingcart;

import java.util.UUID;

public record ProductAddedToCartEvent(
        UUID cartId,
        UUID productId

) {
}
