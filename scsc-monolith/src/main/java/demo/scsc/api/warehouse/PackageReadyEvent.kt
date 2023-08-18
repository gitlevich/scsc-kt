package demo.scsc.api.warehouse;

import java.util.UUID;

public record PackageReadyEvent(
        UUID shipmentId,
        UUID orderId

) {
}
