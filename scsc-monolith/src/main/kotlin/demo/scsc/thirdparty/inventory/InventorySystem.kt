package demo.scsc.thirdparty.inventory

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import demo.scsc.config.AxonFramework.Companion.configure
import org.axonframework.config.Configuration
import java.io.IOException
import java.util.function.Consumer
import javax.swing.SwingUtilities

class InventorySystem {
    private val axonFramework: Configuration
    private val inventory: List<InventoryProduct>

    init {
        inventory = initializeInventory()
        axonFramework = configure("Inventory System")
            .withJsonSerializer()
            .connectedToInspectorAxon("1ca6fe24", "087cb5cb", "c31c8730b7544d82a8a6b7cd114d25f5")
            .start()
        SwingUtilities.invokeLater { InventoryUI(axonFramework, inventory).start() }
    }

    private fun initializeInventory(): MutableList<InventoryProduct> {
        val inventoryProducts: MutableList<InventoryProduct>
        val objectMapper = ObjectMapper()
        val jsonInput = InventorySystem::class.java.getClassLoader().getResourceAsStream("products.json")
        try {
            inventoryProducts = objectMapper.readValue(
                jsonInput,
                object : TypeReference<MutableList<InventoryProduct>>() {})
        } catch (e: IOException) {
            throw IllegalStateException("Can't create External Product System", e)
        }

        return inventoryProducts.map { inventoryProduct: InventoryProduct ->
            inventoryProduct.copy(onSale = true)
        }.toMutableList()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            InventorySystem()
        }
    }
}
