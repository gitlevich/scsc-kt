I think of the original version of this project as a reference implementation, hence design questions. If 
the project's goal is only to demo how to use AF without Spring, please ignore them.  

#### ShoppingCart creates Order aggregate in its checkout command handler, using Order's constructor

```Kotlin
@CommandHandler
fun handle(command: shoppingCart.CheckOutCartCommand) {
    try {
        createNew(Order::class.java) { Order(products, owner) }
    } catch (e: Exception) {
        throw CommandExecutionException(e.message, e)
    }
    applyEvent(shoppingCart.CartCheckedOutEvent(command.cartId))
}
```
Questions:
- Cart aggregate instantiates Order directly. Is this done to avoid publishing CartCheckedOutEvent before we confirmed 
  the Order is created successfully, like a mini transaction + a shorthand for "whenever cart is checked out, create a new order" policy?
- is this a common practice? Given the dedicated method, I assume it is. When is it preferred to doing it via command?
- it looks like the two aggregates live in different contexts. True? If so, would instantiating one from another 
  be a bit too intimate? Or not a concern? This is a tiny project, I am scaling it up in my head to potentially a 
  multi-service implementation where this would be impossible :-)
