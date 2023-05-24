package demo.scsc.api.order;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        String owner,
        List<OrderItem> items
) {
    public record OrderItem(
            UUID id,
            String name,
            BigDecimal price
    ) {
    }
}
