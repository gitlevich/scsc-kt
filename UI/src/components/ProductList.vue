<template>
  <div>
    <q-banner
      v-if="productStore.products.length == 0"
      class="bg-secondary text-white"
    >
      We are sorry but there are no products available at the moment!
      <template v-slot:action>
        <q-btn flat color="white" label="Refresh" @click="reloadPage()" />
      </template>
    </q-banner>

    <div class="row q-pa-lg">
      <div
        v-for="product in productStore.products"
        :key="product.id"
        class="col-3 q-pa-sm"
      >
        <q-card class="fit">
          <img :src="'/products/' + product.image" />

          <q-card-section>
            <div class="text-h6">{{ product.name }}</div>
            <div class="text-subtitle2">{{ product.price }} credits</div>
          </q-card-section>

          <q-card-section class="q-pt-none "  style="height: 50px;">
            {{ product.desc }}
          </q-card-section>

          <q-card-actions vertical align="right">
            <q-btn
              color="primary"
              @click="addProductToCart(product.id)"
              label="Add to cart"
              class="fit"
            />
          </q-card-actions>
        </q-card>
      </div>
    </div>
  </div>
</template>

<script>
import { defineComponent } from "vue";
import { useProductsStore } from "stores/products";
import { useCartStore } from "stores/carts";
import { useUserStore } from "stores/users";
import useQuasar from "quasar/src/composables/use-quasar.js";

export default defineComponent({
  name: "ProductList",

  setup() {
    const productStore = useProductsStore();
    const cartStore = useCartStore();
    const userStore = useUserStore();
    const $q = useQuasar();

    productStore.$subscribe((mutation, state) => {
      if (mutation.events.key === "sortBy") {
        productStore.getProducts();
      }
    });

    function addProductToCart(productId) {
      cartStore
        .addToCart(productId, cartStore.cart.cartId, userStore.user)
        .then(() => {
          $q.notify({
            type: "positive",
            message:
              "Product added to the shopping cart! Refresh the page if cart does not update immediately.",
          });
          cartStore.getUserCarts(userStore.user);
        })
        .catch((error) => {
          $q.notify({
            type: "negative",
            message: "" + error.response.data.error,
          });
        });
    }

    function reloadPage() {
      window.location.reload();
    }

    return {
      productStore,
      addProductToCart,
      reloadPage
    };
  },
});
</script>
