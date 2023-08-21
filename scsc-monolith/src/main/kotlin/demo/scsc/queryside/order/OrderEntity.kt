package demo.scsc.queryside.order

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "Orders")
data class OrderEntity (
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID,

    @Column
    val owner: String,

    @Column
    val isPaid: Boolean = false,

    @Column
    val isPrepared: Boolean = false,

    @Column
    val isReady: Boolean = false,

    @ElementCollection
    @CollectionTable(name = "order_items", joinColumns = [JoinColumn(name = "orderId")])
    @Column(name = "id")
    val items: List<OrderEntityItem> = listOf()
)
