package demo.scsc.config

import org.axonframework.config.*
import org.axonframework.eventhandling.tokenstore.jpa.JpaTokenStore
import org.axonframework.modelling.saga.repository.jpa.JpaSagaStore
import org.axonframework.serialization.json.JacksonSerializer

class AxonFramework private constructor(private val applicationName: String) {
    private val configurer: Configurer
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

    fun startAndWait() {
        configuration = start()
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

    fun connectedToInspectorAxon(workspace: String, environment: String, token: String): AxonFramework {
        InspectorAxonConnection.connect()
            .toWorkspace(workspace)
            .toEnvironment(environment)
            .asApplication(applicationName)
            .withAccessToken(token)
            .start(configurer)
        return this
    }

    companion object {
        private val LOCK = Any()

        fun configure(name: String): AxonFramework = AxonFramework(name)
    }
}
