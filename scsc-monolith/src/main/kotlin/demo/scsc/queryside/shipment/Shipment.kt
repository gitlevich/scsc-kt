package demo.scsc.queryside.shipment

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "shipment")
data class Shipment(
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID,

    @Column
    val recipient: String,

    @ElementCollection
    @CollectionTable(name = "shipment_items", joinColumns = [JoinColumn(name = "shipmentId")])
    @Column(name = "id")
    val items: List<ShipmentItem> = listOf()
)

@Embeddable
data class ShipmentItem(val id: UUID)
