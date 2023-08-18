package demo.scsc.api.warehouse;

import java.util.*

data class GetShippingQueryResponse(
    val items: List<ShippingItem>
) {
    operator fun plus(item: ShippingItem): GetShippingQueryResponse = copy(items = items + item)
    operator fun minus(item: ShippingItem): GetShippingQueryResponse = copy(items = items - item)

    data class ShippingItem(
        val shipmentId: UUID,
        val productId: UUID,
        val removed: Boolean
    )
}
