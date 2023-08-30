package demo.thirdparty.warehouse

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import demo.scsc.config.AxonFramework.Companion.configure
import org.axonframework.config.Configuration
import java.io.IOException
import java.util.*
import java.util.function.Consumer
import javax.swing.SwingUtilities

class WarehouseSystem(appConfig: Config) {
    private val axonFramework: Configuration
    private val inventory: MutableMap<UUID, String> = HashMap()

    init {
        loadInventory()
        axonFramework = configure("Warehouse System", appConfig)
            .withJsonSerializer()
            .connectedToInspectorAxon()
            .start()
        SwingUtilities.invokeLater { WarehouseUI(axonFramework, inventory).start() }
    }

    private fun loadInventory() {
        val objectMapper = ObjectMapper()
        val jsonInput = WarehouseSystem::class.java.getClassLoader().getResourceAsStream("products.json")
        try {
            val arrayNode = objectMapper.readValue(jsonInput, ArrayNode::class.java)
            arrayNode.forEach(
                Consumer { element: JsonNode ->
                    inventory[UUID.fromString(element["id"].asText())] = element["name"].asText()
                }
            )
        } catch (e: IOException) {
            throw IllegalStateException("Can't create External Warehouse System", e)
        }
    }
}

fun main(args: Array<String>) {
    WarehouseSystem(ConfigFactory.load())
}
