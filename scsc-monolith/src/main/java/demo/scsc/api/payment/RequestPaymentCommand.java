package demo.scsc.api.payment;

import java.math.BigDecimal;
import java.util.UUID;

public record RequestPaymentCommand(
        UUID orderPaymentId,
        UUID orderId,
        BigDecimal amount
) {
}
