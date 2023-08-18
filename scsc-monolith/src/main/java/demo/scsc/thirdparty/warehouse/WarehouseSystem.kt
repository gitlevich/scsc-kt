package demo.scsc.thirdparty.warehouse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import demo.scsc.config.AxonFramework;
import org.axonframework.config.Configuration;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WarehouseSystem {

    private final Configuration axonFramework;
    private final Map<UUID, String> inventory = new HashMap<>();

    public WarehouseSystem() {
        loadInventory();

        axonFramework = AxonFramework.configure("Warehouse System")
                .withJsonSerializer()
                .connectedToInspectorAxon("1ca6fe24", "087cb5cb", "c31c8730b7544d82a8a6b7cd114d25f5")
                .start();

        SwingUtilities.invokeLater(() -> new WarehouseUI(axonFramework, inventory).start());
    }

    private void loadInventory() {
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream jsonInput = WarehouseSystem.class.getClassLoader().getResourceAsStream("products.json");
        try {
            ArrayNode arrayNode = objectMapper.readValue(jsonInput, ArrayNode.class);
            arrayNode.forEach(element -> inventory.put(
                    UUID.fromString(element.get("id").asText()),
                    element.get("name").asText())
            );
        } catch (IOException e) {
            throw new IllegalStateException("Can't create External Warehouse System", e);
        }
    }

    public static void main(String[] args) {
        new WarehouseSystem();
    }


}
