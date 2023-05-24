package demo.scsc.thirdparty.warehouse;

import demo.scsc.api.warehouse.AddProductToPackageCommand;
import demo.scsc.api.warehouse.GetShippingQueryResponse;
import org.axonframework.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.Map;
import java.util.UUID;

public class WarehouseUI extends JFrame {

    public static final Logger LOG = LoggerFactory.getLogger(WarehouseUI.class);

    private final Configuration axonFramework;
    private GetShippingQueryResponse queryResponse;
    private final Map<UUID, String> inventory;

    public WarehouseUI(Configuration configuration, Map<UUID, String> inventory) {
        super("Warehouse System");
        this.axonFramework = configuration;
        this.inventory = inventory;

        Container mainContainer = getContentPane();
        mainContainer.setLayout(new BorderLayout());

        ShippingTableModel shippingTableModel = new ShippingTableModel();

        JTable productTable = new JTable(shippingTableModel);
        productTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
        productTable.setDefaultRenderer(UUID.class, new ProductRenderer());
        productTable.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(productTable);

        JButton updateButton = new JButton("Mark as ready");
        updateButton.addActionListener(action -> {
            for (int selectedRow : productTable.getSelectedRows()) {

                axonFramework.commandGateway().send(
                        new AddProductToPackageCommand(
                                (UUID) shippingTableModel.getValueAt(selectedRow, 0),
                                (UUID) shippingTableModel.getValueAt(selectedRow, 1)
//                                    UUID.randomUUID()
                        ),
                        (command, response) -> {
                            if (response.isExceptional()) {
                                JOptionPane.showMessageDialog(
                                        this,
                                        response.exceptionResult().getMessage(),
                                        "Remote system responded with error",
                                        JOptionPane.ERROR_MESSAGE
                                );
                            }
                        });

            }
        });

        mainContainer.add(scrollPane, BorderLayout.CENTER);
        mainContainer.add(updateButton, BorderLayout.SOUTH);
    }

    public void start() {
        pack();
        setVisible(true);
    }


    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            axonFramework.shutdown();
            System.exit(0);
        }
    }

    public class ShippingTableModel extends AbstractTableModel {

        private final String[] columnNames = {
                "Shipment",
                "Product"
        };

        ShippingTableModel() {
            axonFramework.queryGateway().subscriptionQuery(
                            "warehouse:getShippingRequests",
                            "",
                            GetShippingQueryResponse.class,
                            GetShippingQueryResponse.ShippingItem.class
                    )
                    .handle(
                            initialResponse -> queryResponse = initialResponse,
                            updateResponse -> {
                                LOG.info("[ QUERY UPDATE ] " + updateResponse);
                                if (updateResponse.removed()) {
                                    queryResponse.items().remove(updateResponse);
                                    fireTableDataChanged();
                                } else {
                                    queryResponse.items().add(updateResponse);
                                    fireTableDataChanged();
                                }
                            }
                    );
        }

        @Override
        public int getRowCount() {
            if (queryResponse == null) return 0;
            return queryResponse.items().size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return UUID.class;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return switch (columnIndex) {
                case 0 -> queryResponse.items().get(rowIndex).shipmentId();
                case 1 -> queryResponse.items().get(rowIndex).productId();
                default -> null;
            };
        }
    }

    public class ProductRenderer extends JLabel implements TableCellRenderer {

        public ProductRenderer() {
            super.setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            if (column == 1) {
                setText(inventory.get(((UUID) value)));
            } else {
                setText(value.toString());
            }

            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }
            return this;
        }

    }
}
