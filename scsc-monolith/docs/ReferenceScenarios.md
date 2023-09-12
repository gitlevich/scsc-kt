# Reference Scenarios

## Shopping cart lifecycle
The user should be able to add products to the cart and eventually check out. 
The result of a checkout should be a new Order.
The cart should timeout after a period of inactivity to avoid being overwhelmed with abandoned carts.

## Order completion process
Whenever a new Order is created:
- a payment should be requested
- shipment of the products in the cart should be requested

Whenever a payment is received:
- the Order should be marked as paid
- if the package is ready, it should be shipped

Whenever the package is ready to ship:
- the Order should be marked as ready to ship
- if payment is received, the package should be shipped

Whenever the package is shipped:
- the Order should be marked as shipped. This completes the process. 

## Collecting order payment
An Order Payment tracks payments received for a given order. It is created during Order Completion Process, when
the amount due becomes known. Its responsibility is to track when the requested amount was received in full.
Once that happens, it announces that the order is fully paid, triggering the next step in the Order Completion Process.

## Shipping the package

TODO
