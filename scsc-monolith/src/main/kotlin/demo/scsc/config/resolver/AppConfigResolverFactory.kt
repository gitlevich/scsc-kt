package demo.scsc.config.resolver

import com.typesafe.config.Config
import org.axonframework.messaging.Message
import org.axonframework.messaging.annotation.ParameterResolver
import org.axonframework.messaging.annotation.ParameterResolverFactory
import java.lang.reflect.Executable
import java.lang.reflect.Parameter

class AppConfigResolverFactory(private val appConfig: Config) : ParameterResolverFactory {
    override fun createInstance(
        executable: Executable,
        parameters: Array<out Parameter>,
        parameterIndex: Int
    ): ParameterResolver<*> = confResolver

    private val confResolver = object : ParameterResolver<Config> {
        override fun resolveParameterValue(message: Message<*>): Config = appConfig
        override fun matches(message: Message<*>): Boolean = true
    }
}
