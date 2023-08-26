package demo.scsc.queryside.productcatalog

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.*

@Entity
@Table(name = "Products")
class CatalogProduct {
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID? = null

    @Column
    var name: String? = null

    @Column(name = "description")
    var desc: String? = null

    @Column
    var price: BigDecimal? = null

    @Column
    var image: String? = null
}
