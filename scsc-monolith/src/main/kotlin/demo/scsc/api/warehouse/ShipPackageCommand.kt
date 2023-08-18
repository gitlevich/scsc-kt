package demo.scsc.api.warehouse

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.*

data class ShipPackageCommand(@TargetAggregateIdentifier val shipmentId: UUID)
