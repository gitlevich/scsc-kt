package demo.scsc.queryside.warehouse;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "shipping_product")
public class ShippingProductEntity {

    @EmbeddedId
    private Id id;

    public ShippingProductEntity() {
    }

    public ShippingProductEntity(Id id) {
        this.id = id;
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    @Embeddable
    public static class Id implements Serializable {
        private UUID shippingId;
        private UUID productId;

        public Id() {
        }

        public Id(UUID shippingId, UUID productId) {
            this.shippingId = shippingId;
            this.productId = productId;
        }

        public UUID getShippingId() {
            return shippingId;
        }

        public void setShippingId(UUID shippingId) {
            this.shippingId = shippingId;
        }

        public UUID getProductId() {
            return productId;
        }

        public void setProductId(UUID productId) {
            this.productId = productId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Id id = (Id) o;
            return Objects.equals(shippingId, id.shippingId) && Objects.equals(productId, id.productId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(shippingId, productId);
        }
    }
}
