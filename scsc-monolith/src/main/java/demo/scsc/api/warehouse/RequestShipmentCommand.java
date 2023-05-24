package demo.scsc.api.warehouse;

import java.util.List;
import java.util.UUID;

public record RequestShipmentCommand(
        UUID shipmentId,
        UUID orderId,
        String recipient,
        List<UUID> products
) {
}
