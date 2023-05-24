<template>
  <div>
    <q-toolbar class="text-primary q-mb-md">
      <q-toolbar-title class="text-h5"> Shopping cart </q-toolbar-title>
      <q-toggle v-model="refresh" checked-icon="check" unchecked-icon="clear" />
    </q-toolbar>

    <!-- <h5 class="text-primary text-weight-bolder"></h5> -->
    <q-banner v-if="!showCart()" class="bg-transparent text-primary">
      The shopping cart is empty!
    </q-banner>
    <q-list v-if="cartStore.cart" separator>
      <q-item
        class="bg-primary q-mt-xs"
        v-for="product in cartStore.cart.productInfos"
        :key="product.id"
      >
        <q-item-section>
          <q-item-label class="text-white caption">{{
            product.name
          }}</q-item-label>
        </q-item-section>
        <q-item-section side header>
          <q-item-label class="text-white text-weight-bolder">{{
            product.price
          }}</q-item-label>
        </q-item-section>
        <q-item-section top side>
          <q-btn
            class="text-white"
            size="12px"
            flat
            dense
            round
            icon="delete"
            @click="removeProductFromCart(product.id)"
          />
        </q-item-section>
      </q-item>
    </q-list>
    <q-item v-if="showCart()">
      <q-item-section>
        <q-item-label class="text-h6 text-primary">Total:</q-item-label>
      </q-item-section>
      <q-item-section side>
        <q-item-label class="text-h6 text-weight-bolder text-primary">
          {{ cartStore.getTotal() }}</q-item-label
        >
      </q-item-section>
    </q-item>
    <q-btn-group spread v-if="showCart()">
      <q-btn
        rounded
        glossy
        size="md"
        color="blue-grey-4"
        icon="remove_shopping_cart"
        label="Empty cart"
        @click="emptyCart()"
      />
      <q-btn
        rounded
        glossy
        color="accent"
        size="md"
        @click="purchase()"
        icon="shopping_bag"
        label="Order"
      />
    </q-btn-group>
  </div>
</template>

<script>
import { defineComponent, ref, onMounted } from "vue";
import { useCartStore } from "stores/carts";
import { useUserStore } from "stores/users";
import useQuasar from "quasar/src/composables/use-quasar.js";

export default defineComponent({
  name: "ShoppingCart",

  setup() {
    const cartStore = useCartStore();
    const userStore = useUserStore();
    const $q = useQuasar();
    const pollInterval = ref({});
    const refresh = ref(true);

    function showCart() {
      return (
        cartStore.cart &&
        cartStore.cart.products &&
        cartStore.cart.products.length > 0
      );
    }

    function removeProductFromCart(productId) {
      cartStore
        .removeFromCart(productId, cartStore.cart.cartId, userStore.user)
        .then(() => {
          $q.notify({
            type: "positive",
            message:
              "Product removed from the shopping cart! Refresh the page if cart panel does not update immediately.",
          });
        })
        .catch((error) => {
          $q.notify({
            type: "negative",
            message: "Error: " + error.response.data.error,
          });
        });
    }

    function emptyCart() {
      cartStore
        .abandonCart(cartStore.cart.cartId)
        .then(() => {
          $q.notify({
            type: "positive",
            message:
              "Cart is now empty. Refresh the page if cart panel does not update immediately.",
          });
        })
        .catch((error) => {
          $q.notify({
            type: "negative",
            message: "Error: " + error.response.data.error,
          });
        });
    }

    function purchase() {
      cartStore
        .checkOutCart(cartStore.cart.cartId)
        .then(() => {
          $q.notify({
            type: "positive",
            message:
              "Order created! Refresh the page if orders panel does not update immediately.",
          });
        })
        .catch((error) => {
          $q.notify({
            type: "negative",
            message: "Error: " + error.response.data.error,
          });
        });
    }

    function getUserCarts() {
      if (refresh.value) {
        cartStore.getUserCarts(userStore.user);
      }
    }
    onMounted(() => {
      pollInterval.value = setInterval(getUserCarts, 1000); //save reference to the interval
      setTimeout(() => {
        clearInterval(pollInterval.value);
      }, 36000000); //stop polling after an hour
    });

    return {
      cartStore,
      userStore,
      showCart,
      removeProductFromCart,
      emptyCart,
      purchase,
      refresh,
    };
  },
});
</script>
