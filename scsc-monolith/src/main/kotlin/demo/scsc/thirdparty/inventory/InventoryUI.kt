package demo.scsc.thirdparty.inventory

import demo.scsc.api.productcatalog
import org.axonframework.config.Configuration
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.WindowEvent
import java.math.BigDecimal
import java.util.function.Consumer
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.table.AbstractTableModel

class InventoryUI(private val configuration: Configuration, private val inventory: MutableList<InventoryProduct>) :
    JFrame("Product System") {
    init {
        val mainContainer = contentPane
        mainContainer.setLayout(BorderLayout())
        val productTableModel = ProductTableModel()
        val productTable = JTable(productTableModel)
        productTable.preferredScrollableViewportSize = Dimension(500, 70)
        productTable.setFillsViewportHeight(true)
        val scrollPane = JScrollPane(productTable)
        val updateButton = JButton("Publish product(s) update")
        updateButton.addActionListener { action: ActionEvent? ->
            inventory.forEach(
                Consumer { (id, name, desc, price, image, onSale): InventoryProduct ->
                    configuration.eventGateway().publish(
                        productcatalog.ProductUpdateReceivedEvent(
                            id,
                            name,
                            desc,
                            price,
                            image,
                            onSale
                        )
                    )
                }
            )
        }
        mainContainer.add(scrollPane, BorderLayout.CENTER)
        mainContainer.add(updateButton, BorderLayout.SOUTH)
    }

    fun start() {
        pack()
        isVisible = true
    }

    override fun processWindowEvent(e: WindowEvent) {
        super.processWindowEvent(e)
        if (e.id == WindowEvent.WINDOW_CLOSING) {
            configuration.shutdown()
            System.exit(0)
        }
    }

    inner class ProductTableModel : AbstractTableModel() {
        private val columnNames = arrayOf(
            "Name",
            "Description",
            "Image",
            "On sale",
            "price"
        )

        override fun getRowCount(): Int {
            return inventory.size
        }

        override fun getColumnCount(): Int {
            return columnNames.size
        }

        override fun getColumnName(col: Int): String {
            return columnNames[col]
        }

        override fun getColumnClass(columnIndex: Int): Class<*> {
            if (columnIndex == 3) return Boolean::class.java
            return if (columnIndex == 4) BigDecimal::class.java else super.getColumnClass(columnIndex)
        }

        override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
            return columnIndex == 3 || columnIndex == 4
        }

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            val (_, name, desc, price, image, onSale) = inventory[rowIndex]
            return when (columnIndex) {
                0 -> name
                1 -> desc
                2 -> image
                3 -> onSale
                4 -> price
                else -> null
            }!!
        }

        override fun setValueAt(aValue: Any, rowIndex: Int, columnIndex: Int) {
            val inventoryProduct = inventory[rowIndex]
            if (columnIndex == 3) {
                inventory[rowIndex] = inventoryProduct.copy(onSale = (aValue as Boolean))
            }
            if (columnIndex == 4) {
                inventory[rowIndex] = inventoryProduct.copy(price = (aValue as BigDecimal))
            }
        }
    }
}
