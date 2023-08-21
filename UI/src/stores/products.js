import { defineStore } from 'pinia'
import { queryGateway } from "boot/axios";

export const useProductsStore = defineStore('products', {
    state: () => ({
        products: [],
        sortBy: 'name'
    }),
    getters: {
        productMap: (state) => {
            return Object.assign({}, ...state.products.map(s => ({ [s.id]: s })));
        }
    },
    actions: {
        getProducts() {
            queryGateway.post(
                "",
                {
                    "name": "demo.scsc.api.productCatalog.ProductListQuery",
                    "payload": {
                        "sortBy": this.sortBy,
                    },
                    "payloadType": "demo.scsc.api.productCatalog.ProductListQuery",
                    "responseType": "demo.scsc.api.productCatalog.ProductListQueryResponse",
                },
            )
                .then((response) => {
                    if (response.data.payload) {
                        this.products = response.data.payload.products;
                    }
                })
                .catch((error) => {
                    this.products = []
                    console.error(error)
                });
        },
    },
})
