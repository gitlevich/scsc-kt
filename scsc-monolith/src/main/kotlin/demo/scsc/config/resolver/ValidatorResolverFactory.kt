package demo.scsc.config.resolver

import org.axonframework.messaging.Message
import org.axonframework.messaging.annotation.ParameterResolver
import org.axonframework.messaging.annotation.ParameterResolverFactory
import java.lang.reflect.Executable
import java.lang.reflect.Parameter

class ValidatorResolverFactory(private val validatorFactory: ValidatorFactory<*, *>) : ParameterResolverFactory {
    override fun createInstance(
        executable: Executable,
        parameters: Array<out Parameter>,
        parameterIndex: Int
    ): ParameterResolver<*> = object : ParameterResolver<ValidatorFactory<*, *>> {
        override fun resolveParameterValue(message: Message<*>): ValidatorFactory<*, *> = validatorFactory
        override fun matches(message: Message<*>): Boolean = true
    }
}

interface ValidatorFactory<SUBJECT, VALIDATOR> {
    fun validator(subject: SUBJECT): VALIDATOR
}
