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
- ProductValidation is instantiated in Order constructor, not injected. Is there a way to test it like that? I tried
   injecting it by creating another parameter resolver factory, but then it becomes messy with having to push it through
   Cart which really doesn't care about it, and the SCSCArchTest.kt complains. 
