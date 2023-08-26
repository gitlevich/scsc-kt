package demo.scsc.queryside.warehouse

import jakarta.persistence.Embeddable
import java.util.*

@Embeddable
data class ShipmentItem(val id: UUID)
