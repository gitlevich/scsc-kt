package demo.scsc.api.warehouse

import java.util.*

data class PackageReadyEvent(val shipmentId: UUID, val orderId: UUID)
