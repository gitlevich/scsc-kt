package demo.scsc.queryside.productcatalog

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.*

@Entity
@Table(name = "Products")
data class CatalogProduct (
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
