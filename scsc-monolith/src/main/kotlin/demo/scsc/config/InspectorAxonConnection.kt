package demo.scsc.config

import io.axoniq.inspector.AxonInspectorConfigurerModule
import io.axoniq.inspector.AxonInspectorProperties
import org.axonframework.config.Configurer

class InspectorAxonConnection {
    private var name: String? = null
    private var workspace: String? = null
    private var environment: String? = null
    private var accessToken: String? = null
    private var host = "connector.inspector.axoniq.io"
    private var port = 7000
    private var secure = true
    private var threadPoolSize = 1
    private var initialDelay: Long = 0
    fun asApplication(name: String?): InspectorAxonConnection {
        this.name = name
        return this
    }

    fun toHost(host: String): InspectorAxonConnection {
        this.host = host
        return this
    }

    fun toPort(port: Int): InspectorAxonConnection {
        this.port = port
        return this
    }

    fun viaSecureConnection(secure: Boolean): InspectorAxonConnection {
        this.secure = secure
        return this
    }

    fun withThreadPoolSize(threadPoolSize: Int): InspectorAxonConnection {
        this.threadPoolSize = threadPoolSize
        return this
    }

    fun withInitialDelay(initialDelay: Long): InspectorAxonConnection {
        this.initialDelay = initialDelay
        return this
    }

    fun toWorkspace(workspace: String?): InspectorAxonConnection {
        this.workspace = workspace
        return this
    }

    fun toEnvironment(environment: String?): InspectorAxonConnection {
        this.environment = environment
        return this
    }

    fun withAccessToken(accessToken: String?): InspectorAxonConnection {
        this.accessToken = accessToken
        return this
    }

    fun start(configurer: Configurer?) {
        checkNotNull(environment) { "You must provide an environment to connect to" }
        checkNotNull(workspace) { "You must provide a workspace to connect to" }
        checkNotNull(name) { "You must provide a name for this application" }
        checkNotNull(accessToken) { "You must provide an access token" }
        AxonInspectorConfigurerModule(
            AxonInspectorProperties(
                workspace!!,
                environment!!,
                accessToken!!,
                name!!,
                host,
                port,
                secure,
                threadPoolSize,
                initialDelay
            )
        ).configureModule(configurer!!)
    }

    companion object {
        fun connect(): InspectorAxonConnection {
            return InspectorAxonConnection()
        }
    }
}
