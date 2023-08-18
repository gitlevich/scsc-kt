package demo.scsc.queryside.order;

import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(name = "Orders")
public class OrderEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column
    private String owner;

    @Column
    private boolean isPaid;

    @Column
    public boolean isPrepared;

    @Column
    public boolean isReady;

    @ElementCollection
    @CollectionTable(
            name = "order_items",
            joinColumns=@JoinColumn(name="orderId")
    )
    @Column(name="id")
    private List<OrderEntityItem> items = new LinkedList<>();


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public List<OrderEntityItem> getItems() {
        return items;
    }

    public void setItems(List<OrderEntityItem> items) {
        this.items = items;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public boolean isPrepared() {
        return isPrepared;
    }

    public void setPrepared(boolean prepared) {
        isPrepared = prepared;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }
}
