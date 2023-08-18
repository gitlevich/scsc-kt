package demo.scsc.api.productcatalog;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductUpdateReceivedEvent (
     UUID id,
     String name,
     String desc,
     BigDecimal price,
     String image,
     boolean onSale
){
}
