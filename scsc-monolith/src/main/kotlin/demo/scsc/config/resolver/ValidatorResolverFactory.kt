package demo.scsc.config.resolver

import org.axonframework.messaging.Message
import org.axonframework.messaging.annotation.ParameterResolver
import org.axonframework.messaging.annotation.ParameterResolverFactory
import java.lang.reflect.Executable
import java.lang.reflect.Parameter

class ValidatorResolverFactory(private val validator: Validator<*, *>) : ParameterResolverFactory {
    override fun createInstance(
        executable: Executable,
        parameters: Array<out Parameter>,
        parameterIndex: Int
    ): ParameterResolver<*> = object : ParameterResolver<Validator<*, *>> {
        override fun resolveParameterValue(message: Message<*>): Validator<*, *> = validator
        override fun matches(message: Message<*>): Boolean = true
    }
}

typealias Validator<SUBJECT, VALIDATOR> = (SUBJECT) -> VALIDATOR
