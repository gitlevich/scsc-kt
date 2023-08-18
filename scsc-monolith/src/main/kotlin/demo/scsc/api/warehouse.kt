package demo.scsc.api

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.*

object warehouse {
    data class AddProductToPackageCommand(
        @TargetAggregateIdentifier val shipmentId: UUID,
        val productId: UUID
    )

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

    data class PackageReadyEvent(val shipmentId: UUID, val orderId: UUID)

    data class PackageShippedEvent(val shipmentId: UUID)
    data class ProductAddedToPackageEvent(val shipmentId: UUID, val productId: UUID)
    data class RequestShipmentCommand(
        val shipmentId: UUID,
        val orderId: UUID,
        val recipient: String,
        val products: List<UUID>
    )
    enum class ShipmentImpossible {
        NOT_READY
    }
    data class ShipmentRequestedEvent(val shipmentId: UUID, val recipient: String, val products: List<UUID>)
    data class ShipPackageCommand(@TargetAggregateIdentifier val shipmentId: UUID)

}
