package demo.scsc.api.warehouse

import java.util.*

data class ProductAddedToPackageEvent(val shipmentId: UUID, val productId: UUID)
