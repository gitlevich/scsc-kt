package demo.scsc.api.shoppingcart;

import java.util.UUID;

public record CartCreatedEvent(
        UUID id,
        String owner
) {
}
