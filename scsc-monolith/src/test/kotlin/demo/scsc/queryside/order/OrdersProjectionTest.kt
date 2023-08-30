package demo.scsc.queryside.order

import com.typesafe.config.ConfigFactory

class OrdersProjectionTest {
    private val projection = OrdersProjection(ConfigFactory.load("application-test.conf"))

}
