# Reference Scenarios

## Scenario 1: Shopping cart lifecycle
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

## Shipping the package
