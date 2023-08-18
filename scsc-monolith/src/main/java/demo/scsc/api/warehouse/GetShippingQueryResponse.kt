package demo.scsc.api.warehouse;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record GetShippingQueryResponse(
        List<ShippingItem> items

) {
    public record ShippingItem(
            UUID shipmentId,
            UUID productId,
            boolean removed

    ) {
        public ShippingItem(UUID shipmentId, UUID productId) {
            this(shipmentId, productId, false);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ShippingItem that = (ShippingItem) o;
            return shipmentId.equals(that.shipmentId) && productId.equals(that.productId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(shipmentId, productId);
        }
    }
}
