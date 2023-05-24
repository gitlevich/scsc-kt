<template>
  <div>
    <div class="row">
      <div class="col-9 q-pa-xs">
        <product-list></product-list>
      </div>
      <div class="col-3 q-pa-sm bg-blue-grey-2">
        <shopping-cart></shopping-cart>
        <q-separator horizontal />
        <user-orders></user-orders>
      </div>
    </div>
  </div>
</template>

<script>
import { defineComponent } from "vue";

import ProductList from "../components/ProductList.vue";
import ShoppingCart from "../components/ShoppingCart.vue";
import UserOrders from "../components/UserOrders.vue";

export default defineComponent({
  name: "IndexPage",
  components: { ProductList, ShoppingCart, UserOrders },
  setup() {},
});
</script>
