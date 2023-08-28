package demo.scsc.config.resolver

import demo.scsc.commandside.order.ProductValidation
import org.axonframework.messaging.Message
import org.axonframework.messaging.annotation.ParameterResolver
import org.axonframework.messaging.annotation.ParameterResolverFactory
import java.lang.reflect.Executable
import java.lang.reflect.Parameter

class ProductValidationResolverFactory : ParameterResolverFactory {
    override fun createInstance(
        executable: Executable?,
        parameters: Array<out Parameter>?,
        parameterIndex: Int
    ): ParameterResolver<*> = productValidationResolver

    private val productValidationResolver = object : ParameterResolver<ProductValidation> {
        override fun resolveParameterValue(message: Message<*>): ProductValidation = ProductValidation()
        override fun matches(message: Message<*>): Boolean = true
    }
}
