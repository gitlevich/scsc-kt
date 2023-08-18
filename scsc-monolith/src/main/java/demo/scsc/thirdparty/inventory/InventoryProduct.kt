package demo.scsc.thirdparty.inventory;

import java.math.BigDecimal;
import java.util.UUID;

public class InventoryProduct {
    public UUID id;
    public String name;
    public String desc;
    public BigDecimal price;
    public String image;
    public boolean onSale;

    @Override
    public String toString() {
        return "ExternalProduct{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                ", price=" + price +
                ", image='" + image + '\'' +
                ", onSale=" + onSale +
                '}';
    }
}
