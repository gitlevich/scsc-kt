package demo.scsc.api.payment;

import java.util.UUID;

public record OrderFullyPaidEvent (
     UUID orderPaymentId,
     UUID orderId
){

}
