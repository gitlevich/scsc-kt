package demo.scsc.queryside.warehouse

import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.io.Serializable
import java.util.*

@Entity
@Table(name = "shipping_product")
class ShippingProductEntity {

    @EmbeddedId
    var id: Id? = null

    constructor()
    constructor(id: Id?) {
        this.id = id
    }

    @Embeddable
    class Id() : Serializable {

        lateinit var shippingId: UUID

        lateinit var productId: UUID

        constructor(shippingId: UUID, productId: UUID): this() {
            this.shippingId = shippingId
            this.productId = productId
        }

        override fun equals(o: Any?): Boolean {
            if (this === o) return true
            if (o == null || javaClass != o.javaClass) return false
            val id = o as Id
            return shippingId == id.shippingId && productId == id.productId
        }

        override fun hashCode(): Int {
            return Objects.hash(shippingId, productId)
        }
    }
}
