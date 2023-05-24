import { ref } from "vue";
import { defineStore } from 'pinia'
import { commandGateway, queryGateway } from "boot/axios";
import { useProductsStore } from "stores/products";

export const useCartStore = defineStore('carts', () => {

    const productStore = useProductsStore();

    const cart = ref({})

    function getUserCarts(owner) {
        return queryGateway.post(
            "",
            {
                "name": "demo.scsc.api.shoppingcart.GetCartQuery",
                "payload": {
                    "owner": owner
                },
                "payloadType": "demo.scsc.api.shoppingcart.GetCartQuery",
                "responseType": "demo.scsc.api.shoppingcart.GetCartQueryResponse",
            },
        )
            .then((response) => {
                if (response.data.payload) {
                    this.cart = response.data.payload;
                    this.cart.total = 0;
                    this.cart.productInfos = [];
                    this.cart.products.forEach((productId) => {
                        let product = productStore.productMap[productId];
                        this.cart.productInfos.push({
                            "id": productId,
                            "name": product.name,
                            "price": product.price,
                        })
                        this.cart.total += product.price
                    })
                }
            })
            .catch((error) => {
                console.error(error)
                throw error
            })
    };

    function getTotal() {
        return this.cart.total.toFixed(2)
    }

    function addToCart(productId, cartId, owner) {
        return commandGateway.post(
            "",
            {
                "name": "demo.scsc.api.shoppingcart.AddProductToCartCommand",
                "payload": {
                    "productId": productId,
                    "owner": owner,
                    "cartId": cartId,
                },
                "payloadType": "demo.scsc.api.shoppingcart.AddProductToCartCommand",
            },
        )
    }

    function removeFromCart(productId, cartId, owner) {
        return commandGateway.post(
            "",
            {
                "name": "demo.scsc.api.shoppingcart.RemoveProductFromCartCommand",
                "payload": {
                    "productId": productId,
                    "cartId": cartId,
                },
                "payloadType": "demo.scsc.api.shoppingcart.RemoveProductFromCartCommand",
            },
        )
    };

    function abandonCart(cartId) {
        return commandGateway.post(
            "",
            {
                "name": "demo.scsc.api.shoppingcart.AbandonCartCommand",
                "payload": {
                    "cartId": cartId,
                },
                "payloadType": "demo.scsc.api.shoppingcart.AbandonCartCommand",
            },
        )
    };

    function checkOutCart(cartId) {
        return commandGateway.post(
            "",
            {
                "name": "demo.scsc.api.shoppingcart.CheckOutCartCommand",
                "payload": {
                    "cartId": cartId,
                },
                "payloadType": "demo.scsc.api.shoppingcart.CheckOutCartCommand",
            },
        )
    };

    return {
        cart,
        getUserCarts,
        addToCart,
        removeFromCart,
        abandonCart,
        checkOutCart,
        getTotal
    }

})