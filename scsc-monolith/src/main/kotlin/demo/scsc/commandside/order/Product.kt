package demo.scsc.commandside.order

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.*

@Entity
@Table(name = "product")
data class Product(
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID,

    @Column
    val name: String,

    @Column
    val price: BigDecimal, // TODO switch to monetary amount

    @Column
    val isOnSale: Boolean
)
