package demo.scsc.queryside.warehouse;

import jakarta.persistence.*;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "shipping")
public class ShippingEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column
    private String recipient;

    @ElementCollection
    @CollectionTable(
            name = "shipping_items",
            joinColumns = @JoinColumn(name = "shippingId")
    )
    @Column(name = "id")
    private List<ShippingEntityItem> items = new LinkedList<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public List<ShippingEntityItem> getItems() {
        return items;
    }

    public void setItems(List<ShippingEntityItem> items) {
        this.items = items;
    }
}
