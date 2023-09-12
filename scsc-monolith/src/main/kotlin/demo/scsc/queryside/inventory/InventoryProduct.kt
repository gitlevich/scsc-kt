package demo.scsc.queryside.inventory

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.*

@Entity
@Table(name = "InventoryProduct")
data class InventoryProduct (
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID,

    @Column
    val name: String,

    @Column(name = "description")
    val desc: String,

    @Column
    val price: BigDecimal,

    @Column
    val image: String
)
