package demo.scsc.queryside.warehouse

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "shipping")
data class ShippingEntity(
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID,

    @Column
    val recipient: String,

    @ElementCollection
    @CollectionTable(name = "shipping_items", joinColumns = [JoinColumn(name = "shippingId")])
    @Column(name = "id")
    val items: List<ShippingEntityItem> = listOf()
)
