package demo.scsc.api.order;

import java.util.UUID;

public record OrderCompletedEvent(
        UUID orderId
) {
}
