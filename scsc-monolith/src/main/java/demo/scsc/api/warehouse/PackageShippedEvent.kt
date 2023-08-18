package demo.scsc.api.warehouse;

import java.util.UUID;

public record PackageShippedEvent(
        UUID shipmentId
) {
}
