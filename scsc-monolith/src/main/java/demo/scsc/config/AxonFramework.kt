package demo.scsc.config;

import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.eventhandling.tokenstore.jpa.JpaTokenStore;
import org.axonframework.modelling.saga.repository.jpa.JpaSagaStore;
import org.axonframework.serialization.json.JacksonSerializer;

public class AxonFramework {

    private static final Object LOCK = new Object();

    private final Configurer configurer;
    private final String applicationName;
    private Configuration configuration;

    private AxonFramework(String name) {
        applicationName = name;
        System.setProperty("axon.application.name", applicationName);
        configurer = DefaultConfigurer.defaultConfiguration();
    }

    public static AxonFramework configure(String name) {
        return new AxonFramework(name);
    }

    public Configuration start() {
        configuration = configurer.buildConfiguration();
        configuration.start();
        return configuration;
    }

    public void startAndWait() {
        configuration = start();
        try {
            synchronized (LOCK) {
                LOCK.wait();
                configuration.shutdown();
            }
        } catch (InterruptedException e) {
        }
    }


    public void shutdown() {
        if (configuration == null) throw new IllegalStateException("There is no configuration to shutdown");
        configuration.shutdown();
    }

    public AxonFramework withJsonSerializer() {
        final JacksonSerializer jacksonSerializer = JacksonSerializer.defaultSerializer();
        configurer
                .configureSerializer(c -> jacksonSerializer)
                .configureMessageSerializer(c -> jacksonSerializer)
                .configureEventSerializer(c -> jacksonSerializer);
        return this;
    }

    public AxonFramework withJPATokenStoreIn(String persistenceUnitName) {
        JpaPersistenceUnit jpaPersistenceUnit = JpaPersistenceUnit.forName(persistenceUnitName);
        configurer
                .eventProcessing(eventProcessingConfigurer -> {
                    eventProcessingConfigurer.registerDefaultTransactionManager(configuration -> jpaPersistenceUnit.getTransactionManager());
                    eventProcessingConfigurer.registerTokenStore(configuration -> JpaTokenStore.builder()
                            .entityManagerProvider(jpaPersistenceUnit.getEntityManagerProvider())
                            .serializer(JacksonSerializer.defaultSerializer())
                            .build());
                });
        return this;
    }

    public AxonFramework withAggregates(Class<?>... aggregates) {
        for (Class<?> aggregate : aggregates) {
            configurer.configureAggregate(aggregate);
        }
        return this;
    }

    public AxonFramework withMessageHandlers(Object... handlers) {
        for (Object handler : handlers) {
            configurer.registerMessageHandler(c -> handler);
        }
        return this;
    }

    public AxonFramework withJpaSagas(String persistenceUnitName, Class<?>... sagas) {
        JpaPersistenceUnit jpaPersistenceUnit = JpaPersistenceUnit.forName(persistenceUnitName);
        for (Class<?> saga : sagas) {
            configurer.eventProcessing(eventProcessingConfigurer -> eventProcessingConfigurer.registerSaga(
                    saga,
                    sagaConfigurer -> sagaConfigurer.configureSagaStore(
                            c -> JpaSagaStore.builder()
                                    .serializer(c.serializer())
                                    .entityManagerProvider(jpaPersistenceUnit.getEntityManagerProvider())
                                    .build()
                    )
            ));
        }
        return this;
    }

    public AxonFramework connectedToInspectorAxon(String workspace, String environment, String token) {
        InspectorAxonConnection.connect()
                .toWorkspace(workspace)
                .toEnvironment(environment)
                .asApplication(applicationName)
                .withAccessToken(token)
                .start(configurer);
        return this;
    }
}