package demo.scsc.queryside.payment

import demo.scsc.Constants
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.*

@Entity
@Table(name = Constants.PROCESSING_GROUP_PAYMENT)
class Payment {
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID? = null

    @Column(unique = true)
    var orderId: UUID? = null

    @Column(name = "due")
    var requestedAmount: BigDecimal? = null

    @Column(name = "paid")
    var paidAmount: BigDecimal? = null
}
