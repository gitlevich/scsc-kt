package demo.scsc.queryside.warehouse

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "shipping")
class ShippingEntity {
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID? = null

    @Column
    var recipient: String? = null

    @ElementCollection
    @CollectionTable(name = "shipping_items", joinColumns = [JoinColumn(name = "shippingId")])
    @Column(name = "id")
    var items: List<ShippingEntityItem> = LinkedList()
}
