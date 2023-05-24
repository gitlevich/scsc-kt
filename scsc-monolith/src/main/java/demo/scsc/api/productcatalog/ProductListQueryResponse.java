package demo.scsc.api.productcatalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductListQueryResponse(
        List<ProductInfo> products
) {
    public record ProductInfo(
            UUID id,
            String name,
            String desc,
            BigDecimal price,
            String image
    ) {
    }
}
