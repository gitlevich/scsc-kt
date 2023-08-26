package demo.scsc.queryside.order

import jakarta.persistence.Embeddable
import java.math.BigDecimal
import java.util.*

@Embeddable
data class OrderItem(
    val id: UUID,
    val name: String,
    val price: BigDecimal
)






