package demo.scsc.config

import org.assertj.core.api.Assertions.assertThat
import org.axonframework.config.ConfigurationParameterResolverFactory
import org.axonframework.messaging.annotation.MultiParameterResolverFactory
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AxonFrameworkTest {

    @Nested
    inner class CustomParameterResolverFactoryConfiguration {

        @Test
        fun `should register custom parameter resolver factories if given any`() {
            val customParameterResolverFactories = listOf(UuidGenParameterResolverFactory())
            val axonFramework = AxonFramework.configure("test")
                .withCustomParameterResolverFactories(customParameterResolverFactories)

            val configuration = axonFramework.start()
            assertThat(configuration.parameterResolverFactory()).isInstanceOf(MultiParameterResolverFactory::class.java)
            val multiParameterResolverFactory =
                configuration.parameterResolverFactory() as MultiParameterResolverFactory
            assertThat(multiParameterResolverFactory.delegates.map { it::class.qualifiedName })
                .hasSize(3)
                .contains(MultiParameterResolverFactory::class.qualifiedName)
                .contains(ConfigurationParameterResolverFactory::class.qualifiedName)
                .containsAll(customParameterResolverFactories.map { it::class.qualifiedName })
        }

        @Test
        fun `should register no custom parameter resolver factories if given none`() {
            val axonFrameworkWithNoCustomResolverFactories =
                AxonFramework.configure("test").withCustomParameterResolverFactories(emptyList())

            val configuration = axonFrameworkWithNoCustomResolverFactories.start()
            val multiParameterResolverFactory =
                configuration.parameterResolverFactory() as MultiParameterResolverFactory

            assertThat(multiParameterResolverFactory.delegates.map { it::class.qualifiedName })
                .hasSize(18)
                .containsAll(
                    listOf(
                        "org.axonframework.commandhandling.CurrentUnitOfWorkParameterResolverFactory",
                        "org.axonframework.messaging.annotation.InterceptorChainParameterResolverFactory",
                        "org.axonframework.eventhandling.SequenceNumberParameterResolverFactory",
                        "org.axonframework.eventhandling.TimestampParameterResolverFactory",
                        "org.axonframework.messaging.annotation.MessageIdentifierParameterResolverFactory",
                        "org.axonframework.messaging.annotation.SourceIdParameterResolverFactory",
                        "org.axonframework.messaging.annotation.AggregateTypeParameterResolverFactory",
                        "org.axonframework.eventhandling.ConcludesBatchParameterResolverFactory",
                        "org.axonframework.eventhandling.TrackingTokenParameterResolverFactory",
                        "org.axonframework.eventhandling.replay.ReplayParameterResolverFactory",
                        "org.axonframework.eventhandling.replay.ReplayContextParameterResolverFactory",
                        "org.axonframework.messaging.annotation.ResultParameterResolverFactory",
                        "org.axonframework.messaging.annotation.ScopeDescriptorParameterResolverFactory",
                        "org.axonframework.messaging.deadletter.DeadLetterParameterResolverFactory",
                        "org.axonframework.eventsourcing.conflictresolution.ConflictResolution",
                        "org.axonframework.messaging.annotation.DefaultParameterResolverFactory",
                        "org.axonframework.config.ConfigurationParameterResolverFactory",
                        "org.axonframework.test.FixtureResourceParameterResolverFactory"
                    )
                )
        }
    }
}
