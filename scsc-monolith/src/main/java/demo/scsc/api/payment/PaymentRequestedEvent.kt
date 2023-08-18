package demo.scsc.api.payment;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequestedEvent (
     UUID orderPaymentId,
     UUID orderId,
     BigDecimal amount

) {
}
