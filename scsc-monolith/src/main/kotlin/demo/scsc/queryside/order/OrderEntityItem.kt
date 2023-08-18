package demo.scsc.queryside.order

import jakarta.persistence.Embeddable
import java.math.BigDecimal
import java.util.*

@Embeddable
data class OrderEntityItem(
    var id: UUID? = null,
    var name: String? = null,
    var price: BigDecimal? = null
)






