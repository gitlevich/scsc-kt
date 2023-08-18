package demo.scsc.commandside.order

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.*

@Entity
@Table(name = "product_validation")
class ProductValidationEntity {

    @Id
    @Column(name = "id", nullable = false)
    lateinit var id: UUID


    @Column
    lateinit var name: String


    @Column
    lateinit var price: BigDecimal

    @Column
    var isOnSale = false
}
