package demo.scsc.thirdparty.inventory;

import demo.scsc.api.productcatalog.ProductUpdateReceivedEvent;
import org.axonframework.config.Configuration;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.util.List;

public class InventoryUI extends JFrame {

    private final Configuration configuration;

    private final List<InventoryProduct> inventory;

    public InventoryUI(Configuration configuration, List<InventoryProduct> inventory) throws HeadlessException {
        super("Product System");
        this.configuration = configuration;
        this.inventory = inventory;
        Container mainContainer = getContentPane();
        mainContainer.setLayout(new BorderLayout());

        ProductTableModel productTableModel = new ProductTableModel();

        JTable productTable = new JTable(productTableModel);
        productTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
        productTable.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(productTable);

        JButton updateButton = new JButton("Publish product(s) update");
        updateButton.addActionListener(action ->
                inventory.forEach(inventoryProduct ->
                        configuration.eventGateway().publish(
                                new ProductUpdateReceivedEvent(
                                        inventoryProduct.id,
                                        inventoryProduct.name,
                                        inventoryProduct.desc,
                                        inventoryProduct.price,
                                        inventoryProduct.image,
                                        inventoryProduct.onSale
                                )
                        )
                )
        );

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
            configuration.shutdown();
            System.exit(0);
        }
    }

    public class ProductTableModel extends AbstractTableModel {

        private final String[] columnNames = {
                "Name",
                "Description",
                "Image",
                "On sale",
                "price"
        };

        @Override
        public int getRowCount() {
            return inventory.size();
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
            if (columnIndex == 3) return Boolean.class;
            if (columnIndex == 4) return BigDecimal.class;
            return super.getColumnClass(columnIndex);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 3 || columnIndex == 4;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            InventoryProduct inventoryProduct = inventory.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> inventoryProduct.name;
                case 1 -> inventoryProduct.desc;
                case 2 -> inventoryProduct.image;
                case 3 -> inventoryProduct.onSale;
                case 4 -> inventoryProduct.price;
                default -> null;
            };
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 3) {
                inventory.get(rowIndex).onSale = (Boolean) aValue;
            }
            if (columnIndex == 4) {
                inventory.get(rowIndex).price = (BigDecimal) aValue;
            }
        }

    }
}
