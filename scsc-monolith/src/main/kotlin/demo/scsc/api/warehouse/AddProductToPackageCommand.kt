package demo.scsc.api.warehouse

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.*

data class AddProductToPackageCommand(
    @TargetAggregateIdentifier val shipmentId: UUID,
    val productId: UUID
)
