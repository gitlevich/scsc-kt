# Context Map

Our narrative is that of online shopping: a customer adds items to a cart and checks out. The items
constitute an order. The order is paid for and shipped.

We could choose to split this narrative into several coherent models, each focused around an aggregate, 
and bounded in its own context. Then we get the monolith containing these contexts:

- Cart
- Order
- Payment
- Shipment

plus two more contexts for third party services with which we integrate:

- Inventory
- Warehouse

--- INSERT NAIVE CONTEXT MAP PICTURE HERE ---


When I began porting the app, this is how I thought about it. I didn't get a chance to talk to the original authors,
thus missing the reasons behind their design choices. 

Then I drew a context map, and it looked too busy. On the other hand, I discovered a number of clues pointing at
a single context:
- the models I so carefully separated actually all focused on the same thing: shopping
- the code was organized as a monolith. A small one, most likely worked on by a tiny team. 
- package structure: 
  - except for the explicitly separated 3rd party, everything lives under demo.scsc. 
  - demo.scsc package was split, nicely, into API, command side and query side.
- some implementation details bothered me, like the Cart Aggregate directly instantiated an Order Aggregate on checkout. 
  This surprised me: with them being in different contexts, this interaction felt too intimate. So I "improved" design
  by decoupling them:
  - introduced Request Checkout command and Checkout Requested event
  - made Order constructor a handler of the new command 
  - created "whenever cart is checked out" rule that responded to that event by dispatching the command
  While I think it made the code more explicit and easier to test, it also complicated it. A slightly smelly tradeoff. 

So I recognized the original design intent and placed them into a single context. Now the contexts were:

- Shopping
- Inventory
- Warehouse

## Shopping Context

Here, we are working with various aspects of shopping narrative. This context's main human user is the shopper.
Cart, Order, Payment and Shipment encapsulate different aspect of this narrative, expressed as Order Completion Process.

--- INSERT EVENT STORMING PICTURE HERE ---

We integrates with Inventory and Warehouse contexts. Now we can think what these relationships are and characterize them. 
  
--- INSERT CONTEXT MAP PICTURE HERE ---

Finally, we can talk about the internal structure of the Shopping context.

#### Cart
- command model
- query model

#### Order
- command model
- query model

#### Payment
- command model
- query model

#### Shipment
- command model
- query model

### Integrations

#### With Inventory
- downstream from (products are projected in response to events from Inventory)
- maybe Customer-Supplier relationship?

#### With Warehouse
- bidirectional
- 
