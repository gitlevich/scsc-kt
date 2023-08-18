package demo.scsc.queryside.payment;

import demo.scsc.Constants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = Constants.PROCESSING_GROUP_PAYMENT)
public class PaymentEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(unique = true)
    private UUID orderId;

    @Column(name = "due")
    private BigDecimal requestedAmount;

    @Column(name = "paid")
    private BigDecimal paidAmount;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public void setRequestedAmount(BigDecimal requestedAmount) {
        this.requestedAmount = requestedAmount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }
}
