package demo.scsc.queryside.warehouse

import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.io.Serializable
import java.util.*

@Entity
@Table(name = "shipment_product")
data class ShipmentProduct(@EmbeddedId val id: Id) {

    @Embeddable
    data class Id(val shippingId: UUID, val productId: UUID) : Serializable
}
