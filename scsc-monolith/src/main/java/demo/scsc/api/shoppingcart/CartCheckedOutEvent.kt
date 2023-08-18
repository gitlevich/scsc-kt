package demo.scsc.api.shoppingcart;

import java.util.UUID;

public record CartCheckedOutEvent (
     UUID cartId
){
}
