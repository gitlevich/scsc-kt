package demo.scsc.api.shoppingcart;

import java.util.List;
import java.util.UUID;

public record GetCartQueryResponse(
        UUID cartId,
        List<UUID> products
) {
}
