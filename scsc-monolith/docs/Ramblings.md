# Ramblings

As I go through porting the app and understand both the code and the design better, I want to have a place to 
dump my thoughts. They are random, not necessarily related, but they do capture my thought process. I find it
useful because I will be able to look back and see how my understanding of the problem evolved.

This section is such a place. A place for random thoughts.

## Contexts: decomposing the problem

#### The solution has many parts. Should each part be represented by a distinct model?

My reasoning

When I talk about a solution, do these parts fit naturally into a single narrative / sentence? For example,
I can say something like:

```text
The customer adds products to cart, checks out, this results in an order (that the customer can track), containing
the items he ordered, their prices at the moment of placing order, and their quantities. 
Once customer placed the order:
- the payment is requested
- the warehouse is notified
As soon as the requested payment is received, and the warehouse has packaged all the ordered items, the order is shipped.
```

I think this narrative is both coherent and fits nicely into my head. 

Now, lets zoom in to each part.

The cart, order and shipment is what we care about in this scenario. What we need:
- a catalog of products for customer to choose from, including their prices, descriptions, etc. 
- a cart where he adds the chosen products
- the order to keep these product together, as line items
- a way to notify the warehouse about the order so that they could package it
- a way to collect the payment
- a way to tell the warehouse to ship the order

Still fits into my head. Let's draw context boundaries around the model above and call it the Shopping Context.

Now let's see if we can fit more stuff into a the Shopping Context.

To get the catalog of products, we need an inventory management system. It needs a way for people
to enter the products into the system as they come in, track the inventory as products are sold, notify the warehouse when the stock
is low, etc. If we were to stick this inside the Shopping context, it will introduce a sub-narrative that is not relevant 
to shopping. Too much information. Like, you are reading a book, and suddenly the author goes into a long tangent about
some unrelated topic that, as we find out later, was never relevant to the story.

To do the shipping, we need a warehouse management system that can order, receive and store products, package and 
ship orders, deal with order tracking information, returns, etc. Same thing: sounds like another complex narrative, 
so we will not incorporate it into our shopping story. We will deal with it separately. 

So now, we have our Shopping narrative in its Shopping context, and two other black boxes that we know we need, 
but for the purposes of telling the shopping story, we don't care about their internal structure.

***Context sniff test***
```text
Question: does that narrative belong to this context?

 In a context, we tell a single story. Adding another narrative to a story adds details. Details are a kind of complication. 
 If these details clarify and improve the story, the narrative fits. If details obscure the focus instead, it doesn't. 
 If we need to mention it at all, we use an opaque reference, like a quote. A story can become richer by referencing another
 book, but it is unlikely to benefit by embedding it.
 
 We need to reference another context when somehow we need something from it. 
```
