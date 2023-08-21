## Q: How to test explicit aggregate creation in command handler of another aggregate?

Design:
- ShoppingCart creates an order when it is checked out:

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

First, is this a good design choice? Order is a separate aggregate root, wouldn't it be easier to
just have a policy that listens to the CartCheckedOutEvent and creates the Order? This would also
simplify order creation testing.

Second, here are some versions of the test that I tried:

```Kotlin
    @Test
    fun `should publish CartCheckedOutEvent on CheckOutCartCommand`() {
        cart
            .given(
                cartCreatedEvent,
                productAddedToCartEvent,
            )
            .`when`(checkOutCartCommand)
            .expectEvents(cartCheckedOutEvent)
    }

```

Error:

```text
org.axonframework.test.AxonAssertionError: The published events do not match the expected events

Expected                                        |  Actual
------------------------------------------------|------------------------------------------------
demo.scsc.api.shoppingCart$CartCheckedOutEvent <|> 

A probable cause for the wrong chain of events is an exception that occurred while handling the command.
org.axonframework.commandhandling.CommandExecutionException: No product validation available

```

This is because Order instantiates this weird thing called `ProductValidation` in its constructor.
The latter has unclear responsibility as it tries to both be an event handler and a weird business
service responsible for returning yet another thing used to validate products. 

With the above setup, it never had a chance to store any product. I tried publishing ProductUpdateReceivedEvent
in the fixture like this:
```kotlin
cart
   .given(
       cartCreatedEvent,
       productAddedToCartEvent,
   )
   .andGiven(
       productUpdateReceivedEvent
    )
```
but this surely doesn't do anything because I doubt anything in my test is configured to spin up any event
listeners. 


And, if I magically believed that something would spin up that processing group, and publish the event prior to everything:
```Kotlin
@Test
fun `should publish CartCheckedOutEvent on CheckOutCartCommand`() {
    cart
        .given(
            productUpdateReceivedEvent

        )
        .andGiven(
            cartCreatedEvent,
            productAddedToCartEvent,
        )
        .`when`(checkOutCartCommand)
        .expectEvents(cartCheckedOutEvent)
}
```
I get this:

```text
Expected                                        |  Actual
------------------------------------------------|------------------------------------------------
demo.scsc.api.shoppingCart$CartCheckedOutEvent <|> 

A probable cause for the wrong chain of events is an exception that occurred while handling the command.
org.axonframework.eventsourcing.IncompatibleAggregateException: Aggregate identifier must be non-null after applying an event. Make sure the aggregate identifier is initialized at the latest when handling the creation event.
```
because Cart is not yet initialized while I am asking the fixture to process events.


Hence these questions:

- is creating aggregates from another aggregate's command handler when we explicitly separated the contexts
    of the two aggregates a good design choice? Would a command be preferable?
- If this is a legit way to instantiate aggregates, how would one test this?

My opinion:
- not a good design choice, use a policy responding to CartCheckedOutEvent event to send the new `CreateOrderCommand`
- test it with command
- Figure out an alternative to `ProductValidation`, e.g., make it a service injected in the aggregate, like `ProductValidationService`, 
     responsible for answering the question "is this product valid?"
- Have a separate event handler to project the `ProductUpdateReceivedEvent` in the Order context
