package demo.thirdparty.inventory

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import demo.scsc.config.AxonFramework.Companion.configure
import org.axonframework.config.Configuration
import java.io.IOException
import javax.swing.SwingUtilities

class InventorySystem(appConfig: Config) {
    private val axonFramework: Configuration
    private val inventory: List<InventoryProduct>

    init {
        inventory = initializeInventory()
        axonFramework = configure("Inventory System", appConfig)
            .withJsonSerializer()
            .connectedToInspectorAxon()
            .start()
        SwingUtilities.invokeLater { InventoryUI(axonFramework, inventory).start() }
    }

    private fun initializeInventory(): MutableList<InventoryProduct> {
        val inventoryProducts: MutableList<InventoryProduct>
        val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
        val jsonInput = InventorySystem::class.java.getClassLoader().getResourceAsStream("products.json")
            ?: throw IllegalStateException("Can't find products.json")
        try {
            inventoryProducts = objectMapper.readValue(jsonInput)
        } catch (e: IOException) {
            throw IllegalStateException("Can't create External Product System", e)
        }

        return inventoryProducts.map { inventoryProduct: InventoryProduct ->
            inventoryProduct.copy(onSale = true)
        }.toMutableList()
    }
}

@Suppress("UNUSED_PARAMETER")
fun main(args: Array<String>) {
    InventorySystem(ConfigFactory.load())
}
