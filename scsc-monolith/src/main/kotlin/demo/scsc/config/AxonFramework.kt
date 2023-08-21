package demo.scsc.config

import com.typesafe.config.Config
import org.axonframework.config.*
import org.axonframework.eventhandling.tokenstore.jpa.JpaTokenStore
import org.axonframework.messaging.annotation.ClasspathParameterResolverFactory
import org.axonframework.messaging.annotation.MultiParameterResolverFactory
import org.axonframework.messaging.annotation.ParameterResolverFactory
import org.axonframework.modelling.saga.repository.jpa.JpaSagaStore
import org.axonframework.serialization.json.JacksonSerializer
import org.slf4j.LoggerFactory

class AxonFramework private constructor(private val applicationName: String) {
    internal val configurer: Configurer
    private lateinit var configuration: Configuration

    init {
        System.setProperty("axon.application.name", applicationName)
        configurer = DefaultConfigurer.defaultConfiguration()
    }

    fun start(): Configuration {
        configuration = configurer.buildConfiguration()
        configuration.start()
        return configuration
    }

    fun withCustomParameterResolverFactories(customParameterResolverFactories: List<ParameterResolverFactory>): AxonFramework {
        if (customParameterResolverFactories.isNotEmpty()) configurer.registerComponent(ParameterResolverFactory::class.java) {
            LOG.info("Registering custom parameter resolver factories:")
            customParameterResolverFactories.forEach { factory ->
                LOG.info("  - ${factory::class.java.name}")
            }

            MultiParameterResolverFactory(
                listOf(
                    ClasspathParameterResolverFactory.forClass(this::class.java),
                    ConfigurationParameterResolverFactory(configuration)
                ) + customParameterResolverFactories
            )
        }
        return this
    }

    fun startAndWait() {
        configuration = start()
        // VG switch to coroutines
        try {
            synchronized(LOCK) {
                (LOCK as Object).wait()
                configuration.shutdown()
            }
        } catch (_: InterruptedException) {
        }
    }

    fun shutdown() {
        configuration.shutdown()
    }

    fun withJsonSerializer(): AxonFramework {
        val jacksonSerializer = JacksonSerializer.defaultSerializer()
        configurer
            .configureSerializer { jacksonSerializer }
            .configureMessageSerializer { jacksonSerializer }
            .configureEventSerializer { jacksonSerializer }
        return this
    }

    fun withJPATokenStoreIn(persistenceUnitName: String): AxonFramework {
        val jpaPersistenceUnit: JpaPersistenceUnit = jpaPersistenceUnit(persistenceUnitName)
        configurer
            .eventProcessing { eventProcessingConfigurer ->
                eventProcessingConfigurer.registerDefaultTransactionManager { jpaPersistenceUnit.transactionManager }
                eventProcessingConfigurer.registerTokenStore {
                    JpaTokenStore.builder()
                        .entityManagerProvider(jpaPersistenceUnit.entityManagerProvider)
                        .serializer(JacksonSerializer.defaultSerializer())
                        .build()
                }
            }
        return this
    }

    private fun jpaPersistenceUnit(persistenceUnitName: String) = JpaPersistenceUnit.forName(persistenceUnitName)
        ?: throw RuntimeException("No JPA persistence unit found with name $persistenceUnitName")

    fun withAggregates(vararg aggregates: Class<*>): AxonFramework {
        aggregates.forEach { aggregate ->
            configurer.configureAggregate(aggregate)
        }
        return this
    }

    fun withMessageHandlers(vararg handlers: Any): AxonFramework {
        handlers.forEach { handler ->
            configurer.registerMessageHandler { handler }
        }
        return this
    }

    fun withJpaSagas(persistenceUnitName: String, vararg sagas: Class<*>): AxonFramework {
        val jpaPersistenceUnit = jpaPersistenceUnit(persistenceUnitName)
        sagas.forEach { saga ->
            configurer.eventProcessing { eventProcessingConfigurer ->
                eventProcessingConfigurer.registerSaga(saga) { sagaConfigurer ->
                    sagaConfigurer.configureSagaStore {
                        JpaSagaStore.builder()
                            .serializer(it.serializer())
                            .entityManagerProvider(jpaPersistenceUnit.entityManagerProvider)
                            .build()
                    }
                }
            }
        }
        return this
    }

    fun connectedToInspectorAxon(inspectorAxonConfig: Config?): AxonFramework {
        if (inspectorAxonConfig.isInspectorAxonConfigured()) {
            InspectorAxonConnection.connect()
                .toWorkspace(inspectorAxonConfig!!.getString("workspace"))
                .toEnvironment(inspectorAxonConfig.getString("environment"))
                .asApplication(applicationName)
                .withAccessToken(inspectorAxonConfig.getString("token"))
                .start(configurer)
        } else {
            LOG.warn("Inspector Axon connection not configured. Starting without it.")
        }
        return this
    }



    companion object {
        private val LOCK = Any()
        private val LOG = LoggerFactory.getLogger(AxonFramework::class.java)

        fun configure(name: String): AxonFramework = AxonFramework(name)
        fun Config?.isInspectorAxonConfigured(): Boolean = (this != null
                && hasPath("workspace")
                && hasPath("environment")
                && hasPath("token"))}
}
