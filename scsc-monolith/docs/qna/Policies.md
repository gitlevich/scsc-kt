# Policies and Side effects in Aggregate Event Handlers


A Policy, in this context, is a rule triggered by a specific event. I usually formulate a policy using the word
"whenever": whenever an _event_ occurs, it is our policy to _do something_.

I have seen in this project a pattern that is rather tempting: when an Aggregate applies an event from
command handler, (sourcing it for the first time, not replaying), the event handler triggers a side effect. 
In this case, the effect is sending an email:

```Kotlin
    @EventSourcingHandler
    fun on(event: OrderCreatedEvent) {
        orderId = event.orderId
        owner = event.owner
        items.addAll(event.items)
        if (isLive()) try {
            EmailService.sendEmail(
                owner,
                "New order $orderId",
                "Thank you for your order!\n\n$items"
            )
        } catch (e: MessagingException) {
            LOG.error("Failed to send email", e)
        }
    }
```

Nice and compact. 

Additionally, everything runs in a single command thread (I think `AggregateLifecycle.apply(...)` is
executed by command handler as a direct method call on the aggregate instance, but I'm not sure).
Hence, an extra temptation: if email sending fails, so does the command, and the sender learns of it immediately,
via exception mechanism.

I see issues with this approach:
- another non-injected dependency, making it hard to test
- the aggregate is burdened with unrelated responsibility, for sending emails
- success of order creation depends on success of emailing. Unless it was an explicit requirement, 
  we just made domain logic sensitive to an external failure. 

An alternative design is to trigger side effects with policies: say, whenever the order is created, dispatch an email 
to the customer to thank them for the order, etc. Something like this:

```Kotlin
    @DisallowReplay
    @EventHandler
    fun on(event: order.OrderCreatedEvent) {
        log.debug("[   POLICY ] Whenever order is created, send an email to the owner (orderId={})", event.orderId)
        try {
            emailService.sendEmail(
                event.owner,
                "New order ${event.orderId}",
                "Thank you for your order!\n\n${event.items}"
            )
        } catch (e: MessagingException) {
            log.error("Failed to send email", e)
        }
    }
```

It is more verbose, but then it is also completely decoupled from our core business logic. Maybe in other circumstances
we might want to send a text instead. 

Modeling event reactions with policies lets us chunk the narrative into shorter sentences:
- whenever cart checkout is requested, create an order
- whenever order is created, email the owner

In fact, I think we can express almost any scenario in just these three _short_ sentence types:

1. Given an _Aggregate_ receives a _command_, and in its current state it can handle it, it obliges and publishes event(s) about it. 
2. Given an _Aggregate_ receives a _command_, and in its current state it cannot handle it, it rejects the command. 
3. Whenever an _event_ occurs, it is our policy to cause a side effect

One can think of sagas (processes) as policies, but they have a different purpose: they coordinate a transaction over time. 
They do cause side effects, but these are primarily commands to various aggregates. 

As far as Axon implementation, I implement policies (which I often call "rules") as `@EventHandler` methods with `@DisallowReplay`
annotation to ensure it only happens once. See code above.

