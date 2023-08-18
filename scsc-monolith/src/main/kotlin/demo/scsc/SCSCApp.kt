package demo.scsc

import demo.scsc.commandside.order.Order
import demo.scsc.commandside.order.ProductValidation
import demo.scsc.commandside.payment.OrderPayment
import demo.scsc.commandside.shoppingcart.Cart
import demo.scsc.commandside.warehouse.Shipment
import demo.scsc.config.AxonFramework
import demo.scsc.process.OrderCompletionProcess
import demo.scsc.queryside.order.OrdersProjection
import demo.scsc.queryside.payment.PaymentProjection
import demo.scsc.queryside.productcatalog.ProductsProjection
import demo.scsc.queryside.shoppingcart.CartsProjection
import demo.scsc.queryside.warehouse.ShippingProjection

fun main() {
    AxonFramework.configure("SCSC App")
        .withJsonSerializer()
        .withJPATokenStoreIn("SCSC")
        .withAggregates(
            Cart::class.java,
            Order::class.java,
            OrderPayment::class.java,
            Shipment::class.java
        )
        .withJpaSagas(
            "SCSC",
            OrderCompletionProcess::class.java
        )
        .withMessageHandlers(
            ProductsProjection(),
            CartsProjection(),
            OrdersProjection(),
            ProductValidation(),
            PaymentProjection(),
            ShippingProjection()
        )
        .connectedToInspectorAxon("1ca6fe24", "087cb5cb", "c31c8730b7544d82a8a6b7cd114d25f5")
        .startAndWait()
}
