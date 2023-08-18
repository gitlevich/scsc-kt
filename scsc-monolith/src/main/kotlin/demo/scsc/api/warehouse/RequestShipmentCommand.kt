package demo.scsc.api.warehouse

import java.util.*

data class RequestShipmentCommand(
    val shipmentId: UUID,
    val orderId: UUID,
    val recipient: String,
    val products: List<UUID>
)
