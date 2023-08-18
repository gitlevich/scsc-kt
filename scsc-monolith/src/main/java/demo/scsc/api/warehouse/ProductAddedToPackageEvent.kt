package demo.scsc.api.warehouse;

import java.util.UUID;

public record ProductAddedToPackageEvent(
        UUID shipmentId,
        UUID productId

) {
}
