package demo.scsc.api.shoppingcart;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record CartAbandonedEvent(
        UUID cartId,
        @NotNull Reason reason
) {
    public enum Reason {
        MANUAL,
        TIMEOUT
    }


}
