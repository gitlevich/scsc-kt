package demo.scsc.queryside.payment

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.*

@Entity
@Table(name = "Payments")
data class Payment(
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID,

    @Column(unique = true)
    val orderId: UUID,

    @Column(name = "due")
    val requestedAmount: BigDecimal,

    @Column(name = "paid")
    val paidAmount: BigDecimal
) {
    fun withPaidAmount(amount: BigDecimal) = copy(paidAmount = paidAmount + amount)
}
