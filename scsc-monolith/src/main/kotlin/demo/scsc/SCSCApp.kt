package demo.scsc

import com.typesafe.config.ConfigFactory
import demo.scsc.Constants.SCSC
import demo.scsc.commandside.order.Order
import demo.scsc.commandside.order.ProductValidation
import demo.scsc.commandside.payment.OrderPayment
import demo.scsc.commandside.shoppingcart.Cart
import demo.scsc.commandside.warehouse.Shipment
import demo.scsc.config.AxonFramework
import demo.scsc.config.resolver.UuidGenParameterResolverFactory
import demo.scsc.process.OrderCompletionProcess
import demo.scsc.queryside.order.OrdersProjection
import demo.scsc.queryside.payment.PaymentProjection
import demo.scsc.queryside.productcatalog.ProductsProjection
import demo.scsc.queryside.shoppingcart.CartsProjection
import demo.scsc.queryside.warehouse.ShippingProjection

fun main() {
    val appConfig = ConfigFactory.load()

    AxonFramework.configure("$SCSC App")
        .withJsonSerializer()
        .withJPATokenStoreIn(SCSC)
        .withAggregates(
            Cart::class.java,
            Order::class.java,
            OrderPayment::class.java,
            Shipment::class.java
        )
        .withJpaSagas(SCSC, OrderCompletionProcess::class.java)
        .withMessageHandlers(
            ProductsProjection(),
            CartsProjection(),
            OrdersProjection(),
            ProductValidation(),
            PaymentProjection(),
            ShippingProjection()
        )
        .withCustomParameterResolverFactories(listOf(UuidGenParameterResolverFactory()))
        .connectedToInspectorAxon(appConfig.getConfig("application.axon.inspector"))
        .startAndWait()
}
