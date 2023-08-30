package demo.scsc

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import demo.scsc.Constants.SCSC
import demo.scsc.commandside.order.Order
import demo.scsc.commandside.order.ProductValidation
import demo.scsc.commandside.payment.OrderPayment
import demo.scsc.commandside.shoppingcart.Cart
import demo.scsc.commandside.shoppingcart.WheneverCheckoutIsIRequested
import demo.scsc.commandside.warehouse.Shipment
import demo.scsc.config.AxonFramework
import demo.scsc.config.resolver.UuidGenParameterResolverFactory
import demo.scsc.process.OrderCompletionProcess
import demo.scsc.queryside.order.OrdersProjection
import demo.scsc.queryside.payment.PaymentProjection
import demo.scsc.queryside.productcatalog.ProductsProjection
import demo.scsc.queryside.shoppingcart.CartsProjection
import demo.scsc.queryside.warehouse.ShipmentProjection

fun main(args: Array<String>) {
    val appConfig: Config = ConfigFactory.load()
    AxonFramework.configure("$SCSC App", appConfig)
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
            ProductsProjection(appConfig),
            CartsProjection(),
            OrdersProjection(appConfig),
            ProductValidation(appConfig),
            PaymentProjection(appConfig),
            ShipmentProjection(appConfig),
            WheneverCheckoutIsIRequested()
        )
        .withCustomParameterResolverFactories(
            listOf(
                UuidGenParameterResolverFactory(),
            )
        )
        .connectedToInspectorAxon()
        .startAndWait()
}
