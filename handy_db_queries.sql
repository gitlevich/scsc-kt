-- Drop tables 
DROP TABLE associationvalueentry;
DROP TABLE order_items;
DROP TABLE order_payments;
DROP TABLE orders;
DROP TABLE product_validation;
DROP TABLE products;
DROP TABLE sagaentry;
DROP TABLE shipping CASCADE;
DROP TABLE shipping_items;
DROP TABLE shipping_product;
DROP TABLE tokenentry;

-- List tokens
SELECT * FROM tokenentry
SELECT *, convert_from(lo_get(token), 'UTF-8') FROM tokenentry
ORDER BY processorname ASC, segment ASC 

-- List sagas
SELECT * FROM sagaentry
SELECT *, convert_from(lo_get(serializedsaga), 'UTF-8') FROM sagaentry
SELECT * FROM associationvalueentry


-- List products
SELECT * FROM products
SELECT * FROM "product-validation"

-- List orders
SELECT * FROM orders

SELECT o.id, o.owner, i.name, i.price FROM orders o
LEFT JOIN order_items i ON o.id =i.orderId

SELECT * FROM order_payments

-- List shippings
SELECT * FROM shipping
SELECT * FROM shipping_product

