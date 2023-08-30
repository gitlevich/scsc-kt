package demo.scsc

import demo.scsc.Order.createOrderCommand
import demo.scsc.Order.orderId
import demo.scsc.api.order
import demo.scsc.api.payment
import demo.scsc.api.shoppingcart
import demo.scsc.api.warehouse
import demo.scsc.commandside.warehouse.Shipment
import org.axonframework.eventhandling.DomainEventMessage
import org.axonframework.eventhandling.GenericDomainEventMessage
import org.axonframework.messaging.MetaData
import java.math.BigDecimal
import java.util.*

object Order {
    val orderId: UUID = UUID.randomUUID()
    val product1: UUID = UUID.randomUUID()
    val createOrderCommand = order.CreateOrderCommand(
        owner = "John Doe",
        itemIds = listOf(product1)
    )
    val orderCreatedEvent = order.OrderCreatedEvent(
        orderId = orderId,
        owner = createOrderCommand.owner,
        items = createOrderCommand.itemIds.map {
            order.OrderCreatedEvent.OrderItem(it, "name", BigDecimal.valueOf(1.0))
        }
    )
    val completeOrderCommand = order.CompleteOrderCommand(orderId)
    val orderCompletedEvent = order.OrderCompletedEvent(orderId = orderId)
}

object Payment {
    val requestedPaymentCommand = payment.RequestPaymentCommand(
        orderPaymentId = UUID.randomUUID(),
        orderId = orderId,
        amount = BigDecimal.TEN
    )
    val processPaymentCommand = payment.ProcessPaymentCommand(
        orderPaymentId = requestedPaymentCommand.orderPaymentId,
        amount = requestedPaymentCommand.amount
    )
    val paymentRequestedEvent = payment.PaymentRequestedEvent(
        orderPaymentId = requestedPaymentCommand.orderPaymentId,
        orderId = requestedPaymentCommand.orderId,
        amount = requestedPaymentCommand.amount
    )
    val paymentReceivedEvent = payment.PaymentReceivedEvent(
        orderPaymentId = requestedPaymentCommand.orderPaymentId,
        amount = requestedPaymentCommand.amount
    )
    val orderFullyPaidEvent = payment.OrderFullyPaidEvent(
        orderPaymentId = requestedPaymentCommand.orderPaymentId,
        orderId = requestedPaymentCommand.orderId
    )
}

object ShoppingCart {
    val cartId: UUID = UUID.fromString("00000000-0000-0000-0000-0c09a0d0d88e")

    val addProductToCartCommand = shoppingcart.AddProductToCartCommand(
        cartId = cartId,
        productId = Order.product1,
        owner = createOrderCommand.owner
    )
    val cartCreatedEvent = shoppingcart.CartCreatedEvent(
        id = cartId,
        owner = addProductToCartCommand.owner
    )
    val productAddedToCartEvent = shoppingcart.ProductAddedToCartEvent(
        cartId = cartId,
        productId = addProductToCartCommand.productId
    )

    val removeProductFromCartCommand = shoppingcart.RemoveProductFromCartCommand(
        cartId = cartId,
        productId = addProductToCartCommand.productId
    )
    val productRemovedFromCartEvent = shoppingcart.ProductRemovedFromCartEvent(
        cartId = cartId,
        productId = addProductToCartCommand.productId
    )

    val abandonCartCommand = shoppingcart.AbandonCartCommand(
        cartId = cartId
    )
    val cartAbandonedEvent = shoppingcart.CartAbandonedEvent(
        cartId = cartId,
        reason = shoppingcart.CartAbandonedEvent.Reason.TIMEOUT
    )

    val checkOutCartCommand = shoppingcart.CheckOutCartCommand(cartId = cartId)
    val cartCheckoutRequestedEvent = shoppingcart.CartCheckoutRequestedEvent(
        cartId = cartId,
        owner = addProductToCartCommand.owner,
        products = listOf(addProductToCartCommand.productId)
    )

    val completeCartCheckoutCommand = shoppingcart.CompleteCartCheckoutCommand(
        cartId = cartId,
        orderId = orderId
    )
    val cartCheckoutCompletedEvent = shoppingcart.CartCheckoutCompletedEvent(cartId = cartId)

    val handleCheckoutFailureCommand = shoppingcart.HandleCartCheckoutFailureCommand(cartId = cartId)
    val checkoutFailedEvent = shoppingcart.CartCheckoutFailedEvent(cartId = cartId)

}

object Warehouse {
    private val shipmentId: UUID = UUID.randomUUID()
    private val recipient: String = createOrderCommand.owner
    private val products: List<UUID> = listOf(Order.product1)
    val requestShipmentCommand = warehouse.RequestShipmentCommand(
        shipmentId,
        orderId, recipient, products
    )
    val shipPackageCommand = warehouse.ShipPackageCommand(shipmentId)
    val shipmentRequestedEvent = warehouse.ShipmentRequestedEvent(shipmentId, recipient, products)
    val packageShippedEvent = warehouse.PackageShippedEvent(shipmentId)
    val addProductToPackageCommand = warehouse.AddProductToPackageCommand(shipmentId, products[0])
    val productAddedAToPackageEvent = warehouse.ProductAddedToPackageEvent(shipmentId, products[0])
    val packageReadyEvent = warehouse.PackageReadyEvent(shipmentId, orderId)
    val shipmentRequestedEventMessage: DomainEventMessage<warehouse.ShipmentRequestedEvent> =
        GenericDomainEventMessage(
            Shipment::class.java.name,
            shipmentId.toString(),
            0, // sequence number
            shipmentRequestedEvent
        ).andMetaData(MetaData.with("orderId", orderId))
}
