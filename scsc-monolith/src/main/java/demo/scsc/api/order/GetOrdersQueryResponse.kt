package demo.scsc.api.order;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record GetOrdersQueryResponse(
        List<Order> orders
) {

    public record Order(
            UUID id,
            BigDecimal total,
            List<OrderLine> lines,
            String owner,
            boolean isPaid,
            boolean isPrepared,
            boolean isShipped
    ) {
    }

    public record OrderLine(
            String name,
            BigDecimal price
    ) {
    }
}
