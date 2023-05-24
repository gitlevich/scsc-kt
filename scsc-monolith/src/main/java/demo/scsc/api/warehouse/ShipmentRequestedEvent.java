package demo.scsc.api.warehouse;

import java.util.List;
import java.util.UUID;

public record ShipmentRequestedEvent(
        UUID shipmentId,
        String recipient,
        List<UUID> products

) {
}
