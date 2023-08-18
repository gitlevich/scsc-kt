package demo.scsc.thirdparty.warehouse

import demo.scsc.api.Warehouse.AddProductToPackageCommand
import demo.scsc.api.Warehouse.GetShippingQueryResponse
import demo.scsc.api.Warehouse.GetShippingQueryResponse.ShippingItem
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.commandhandling.CommandResultMessage
import org.axonframework.config.Configuration
import org.slf4j.LoggerFactory
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.WindowEvent
import java.util.*
import javax.swing.*
import javax.swing.table.AbstractTableModel
import javax.swing.table.TableCellRenderer

class WarehouseUI(private val axonFramework: Configuration, private val inventory: Map<UUID, String>) :
    JFrame("Warehouse System") {
    private var queryResponse: GetShippingQueryResponse? = null

    init {
        val mainContainer = contentPane
        mainContainer.setLayout(BorderLayout())
        val shippingTableModel = ShippingTableModel()
        val productTable = JTable(shippingTableModel)
        productTable.preferredScrollableViewportSize = Dimension(500, 70)
        productTable.setDefaultRenderer(UUID::class.java, ProductRenderer())
        productTable.setFillsViewportHeight(true)
        val scrollPane = JScrollPane(productTable)
        val updateButton = JButton("Mark as ready")
        updateButton.addActionListener { action: ActionEvent? ->
            for (selectedRow in productTable.selectedRows) {
                axonFramework.commandGateway().send<AddProductToPackageCommand?, Any>(
                    AddProductToPackageCommand(
                        (shippingTableModel.getValueAt(selectedRow, 0) as UUID),
                        (shippingTableModel.getValueAt(
                            selectedRow,
                            1
                        ) as UUID) //                                    UUID.randomUUID()
                    )
                ) { command: CommandMessage<out AddProductToPackageCommand?>?, response: CommandResultMessage<*> ->
                    if (response.isExceptional) {
                        JOptionPane.showMessageDialog(
                            this,
                            response.exceptionResult().message,
                            "Remote system responded with error",
                            JOptionPane.ERROR_MESSAGE
                        )
                    }
                }
            }
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
            axonFramework.shutdown()
            System.exit(0)
        }
    }

    inner class ShippingTableModel internal constructor() : AbstractTableModel() {
        private val columnNames = arrayOf(
            "Shipment",
            "Product"
        )

        init {
            axonFramework.queryGateway().subscriptionQuery(
                "warehouse:getShippingRequests",
                "",
                GetShippingQueryResponse::class.java,
                ShippingItem::class.java
            )
                .handle(
                    { initialResponse: GetShippingQueryResponse -> queryResponse = initialResponse }
                ) { updateResponse: ShippingItem ->
                    LOG.info("[ QUERY UPDATE ] $updateResponse")
                    val response = queryResponse!!
                    if (updateResponse.removed) {
                        response.items - updateResponse
                        fireTableDataChanged()
                    } else {
                        response.items + updateResponse
                        fireTableDataChanged()
                    }
                }
        }

        override fun getRowCount(): Int {
            return queryResponse?.items?.size ?: 0
        }

        override fun getColumnCount(): Int {
            return columnNames.size
        }

        override fun getColumnName(col: Int): String {
            return columnNames[col]
        }

        override fun getColumnClass(columnIndex: Int): Class<*> {
            return UUID::class.java
        }

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            return when (columnIndex) {
                0 -> queryResponse!!.items.get(rowIndex).shipmentId
                1 -> queryResponse!!.items.get(rowIndex).productId
                else -> null
            }!!
        }
    }

    inner class ProductRenderer : JLabel(), TableCellRenderer {
        init {
            super.setOpaque(true)
        }

        override fun getTableCellRendererComponent(
            table: JTable, value: Any, isSelected: Boolean,
            hasFocus: Boolean, row: Int, column: Int
        ): Component {
            if (column == 1) {
                setText(inventory[value as UUID])
            } else {
                setText(value.toString())
            }
            if (isSelected) {
                setBackground(table.selectionBackground)
                setForeground(table.selectionForeground)
            } else {
                setBackground(table.getBackground())
                setForeground(table.getForeground())
            }
            return this
        }
    }

    companion object {
        val LOG = LoggerFactory.getLogger(WarehouseUI::class.java)
    }
}
