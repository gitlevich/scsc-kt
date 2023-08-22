package demo.scsc.config.resolver

import org.axonframework.messaging.Message
import org.axonframework.messaging.annotation.ParameterResolver
import org.axonframework.messaging.annotation.ParameterResolverFactory
import java.lang.reflect.Executable
import java.lang.reflect.Parameter
import java.util.*

class UuidGenParameterResolverFactory : ParameterResolverFactory {
    override fun createInstance(
        executable: Executable,
        parameters: Array<out Parameter>,
        parameterIndex: Int
    ): ParameterResolver<*> = nextUuid

    private val nextUuid = object : ParameterResolver<UUID> {
        override fun resolveParameterValue(message: Message<*>): UUID = UUID.randomUUID()
        override fun matches(message: Message<*>): Boolean = true
    }
}
