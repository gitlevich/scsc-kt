package demo.scsc.queryside.order

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "Orders")
class OrderEntity {
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID? = null

    @Column
    var owner: String? = null

    @Column
    var isPaid = false

    @Column
    var isPrepared = false

    @Column
    var isReady = false

    @ElementCollection
    @CollectionTable(name = "order_items", joinColumns = [JoinColumn(name = "orderId")])
    @Column(name = "id")
    var items: List<OrderEntityItem> = emptyList()
}
