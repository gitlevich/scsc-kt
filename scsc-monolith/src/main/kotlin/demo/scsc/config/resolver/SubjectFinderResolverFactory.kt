package demo.scsc.config.resolver

import org.axonframework.messaging.Message
import org.axonframework.messaging.annotation.ParameterResolver
import org.axonframework.messaging.annotation.ParameterResolverFactory
import java.lang.reflect.Executable
import java.lang.reflect.Parameter

class SubjectFinderResolverFactory(private val finder: Finder<*, *>) : ParameterResolverFactory {
    override fun createInstance(
        executable: Executable,
        parameters: Array<out Parameter>,
        parameterIndex: Int
    ): ParameterResolver<*> = object : ParameterResolver<Finder<*, *>> {
        override fun resolveParameterValue(message: Message<*>): Finder<*, *> = finder
        override fun matches(message: Message<*>): Boolean = true
    }
}

typealias Finder<IDENTIFIER, SUBJECT> = (IDENTIFIER) -> SUBJECT
