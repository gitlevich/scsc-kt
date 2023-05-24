import { ref } from "vue";
import { defineStore } from 'pinia'
import { commandGateway, queryGateway } from "boot/axios";

export const useOrderStore = defineStore('orders', {
    state: () => ({
        orders: ref([]),
    }),

    actions: {
        getUserOrders(owner) {
            return queryGateway.post(
                "",
                {
                    "name": "demo.scsc.api.order.GetOrdersQuery",
                    "payload": {
                        "owner": owner
                    },
                    "payloadType": "demo.scsc.api.order.GetOrdersQuery",
                    "responseType": "demo.scsc.api.order.GetOrdersQueryResponse",

                },
            )
                .then((response) => {
                    if (response.data.payload) {
                        this.orders = response.data.payload.orders;
                    }
                })
                .catch((error) => {
                    console.error(error)
                    throw error
                })
        },
    },
})