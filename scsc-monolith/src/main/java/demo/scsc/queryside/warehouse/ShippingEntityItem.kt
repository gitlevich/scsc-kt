package demo.scsc.queryside.warehouse;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public class ShippingEntityItem {

    private UUID id;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

}
