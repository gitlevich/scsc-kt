package demo.scsc.api.payment;

import java.util.UUID;

public record GetPaymentForOrderQuery(
        UUID orderId
) {
}
