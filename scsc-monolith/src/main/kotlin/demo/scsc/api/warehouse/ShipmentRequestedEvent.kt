package demo.scsc.api.warehouse

import java.util.*

data class ShipmentRequestedEvent(val shipmentId: UUID, val recipient: String, val products: List<UUID>)
