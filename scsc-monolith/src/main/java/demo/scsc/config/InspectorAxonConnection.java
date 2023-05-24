package demo.scsc.config;

import io.axoniq.inspector.AxonInspectorConfigurerModule;
import io.axoniq.inspector.AxonInspectorProperties;
import org.axonframework.config.Configurer;

public class InspectorAxonConnection {


    private String name;
    private String workspace;
    private String environment;
    private String accessToken;

    private String host = "connector.inspector.axoniq.io";
    private int port = 7000;
    private boolean secure = true;
    private int threadPoolSize = 1;
    private long initialDelay = 0;


    public static InspectorAxonConnection connect () {
        return new InspectorAxonConnection();
    }


    public InspectorAxonConnection asApplication (String name) {
        this.name = name;
        return this;
    }

    public InspectorAxonConnection toHost (String host) {
        this.host = host;
        return this;
    }

    public InspectorAxonConnection toPort (int port) {
        this.port = port;
        return this;
    }

    public InspectorAxonConnection viaSecureConnection (boolean secure) {
        this.secure = secure;
        return this;
    }

    public InspectorAxonConnection withThreadPoolSize (int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
        return this;
    }

    public InspectorAxonConnection withInitialDelay (long initialDelay) {
        this.initialDelay = initialDelay;
        return this;
    }

    public InspectorAxonConnection toWorkspace (String workspace) {
        this.workspace = workspace;
        return this;
    }

    public InspectorAxonConnection toEnvironment (String environment) {
        this.environment = environment;
        return this;
    }

    public InspectorAxonConnection withAccessToken (String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public void start (Configurer configurer) {

        if (environment == null) throw new IllegalStateException("You must provide an environment to connect to");
        if (workspace == null) throw new IllegalStateException("You must provide a workspace to connect to");
        if (name == null) throw new IllegalStateException("You must provide a name for this application");
        if (accessToken == null) throw new IllegalStateException("You must provide an access token");

        new AxonInspectorConfigurerModule(
                new AxonInspectorProperties(
                        workspace,
                        environment,
                        accessToken,
                        name,
                        host,
                        port,
                        secure,
                        threadPoolSize,
                        initialDelay
                )
        ).configureModule(configurer);

    }
}
