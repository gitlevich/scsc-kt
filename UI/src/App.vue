<template>
  <router-view />
</template>

<script>
import { defineComponent, ref, onMounted, watch } from "vue";
import { storeToRefs } from 'pinia'
import { useProductsStore } from "stores/products";
import { useUserStore } from "stores/users";
import { useCartStore } from "stores/carts";
import { useOrderStore } from "stores/orders";

export default defineComponent({
  name: "App",
  setup() {
    const productStore = useProductsStore();
    const userStore = useUserStore();
    const cartStore = useCartStore();
    const orderStore = useOrderStore();

    const { user } = storeToRefs(userStore);

    function getUserRelatedData () {
      cartStore.getUserCarts(userStore.user)
      orderStore.getUserOrders(userStore.user)
    }

    watch(user, async (newUser, oldUser) => {
      cartStore.getUserCarts(newUser);
      orderStore.getUserOrders(newUser)
    });

    onMounted(() => {
      productStore.getProducts();
      getUserRelatedData();
    });

    return {
    }
  },
});
</script>
