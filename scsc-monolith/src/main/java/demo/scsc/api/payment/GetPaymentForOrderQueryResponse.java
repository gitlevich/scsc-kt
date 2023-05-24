package demo.scsc.api.payment;

import java.math.BigDecimal;
import java.util.UUID;

public record GetPaymentForOrderQueryResponse(
        UUID id,
        UUID orderId,
        BigDecimal requestedAmount,
        BigDecimal paidAmount
) {
}
