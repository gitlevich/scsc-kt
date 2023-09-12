package demo.scsc

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import demo.scsc.Constants.SCSC
import demo.scsc.commandside.order.Order
import demo.scsc.commandside.order.ProductService
import demo.scsc.commandside.order.WheneverOrderIsCreated
import demo.scsc.commandside.payment.OrderPayment
import demo.scsc.commandside.shoppingcart.Cart
import demo.scsc.commandside.shoppingcart.WheneverCheckoutIsIRequested
import demo.scsc.commandside.shipment.Shipment
import demo.scsc.config.AxonFramework
import demo.scsc.config.resolver.AppConfigResolverFactory
import demo.scsc.config.resolver.UuidGenResolverFactory
import demo.scsc.config.resolver.SubjectFinderResolverFactory
import demo.scsc.infra.EmailService
import demo.scsc.process.OrderCompletionProcess
import demo.scsc.queryside.order.OrdersProjection
import demo.scsc.queryside.payment.PaymentProjection
import demo.scsc.queryside.inventory.ProductsProjection
import demo.scsc.queryside.shoppingcart.CartsProjection
import demo.scsc.queryside.shipment.ShipmentProjection

@Suppress("UNUSED_PARAMETER")
fun main(args: Array<String>) {
    val appConfig: Config = ConfigFactory.load()
    val productService = ProductService(appConfig)

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
            productService,
            PaymentProjection(appConfig),
            ShipmentProjection(appConfig),
            WheneverCheckoutIsIRequested(),
            WheneverOrderIsCreated(EmailService)
        )
        .withCustomParameterResolverFactories(
            listOf(
                UuidGenResolverFactory(),
                AppConfigResolverFactory(appConfig),
                SubjectFinderResolverFactory(productService)
            )
        )
        .connectedToInspectorAxon()
        .startAndWait()
}
