package demo.scsc.queryside.warehouse

import jakarta.persistence.Embeddable
import java.util.*

@Embeddable
class ShippingEntityItem {
    var id: UUID? = null
}
