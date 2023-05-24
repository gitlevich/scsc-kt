package demo.scsc.api.payment;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.math.BigDecimal;
import java.util.UUID;

public record ProcessPaymentCommand(
        @TargetAggregateIdentifier UUID orderPaymentId,
        BigDecimal amount
) {

}
