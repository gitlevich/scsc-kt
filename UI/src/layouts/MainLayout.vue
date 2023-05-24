<template>
  <q-layout view="hHh lpR fFf">
    <q-header reveal elevated class="bg-primary text-white">
      <q-toolbar>
        <q-toolbar-title>
          <q-avatar>
            <img src="~assets/logo.png" />
          </q-avatar>
          Simply a Complex Shopping Cart
        </q-toolbar-title>
        <q-select
          v-model="productStore.sortBy"
          :options="sortOptions"
          label="Sort"
          filled
          bg-color="secondary"
        />
        <q-separator vertical spaced color="none"></q-separator>
        <q-select
          filled
          bg-color="warning"
          v-model="userStore.user"
          :options="userStore.users"
        >
          <template v-slot:prepend>
            <q-icon name="account_circle" />
          </template>
        </q-select>
      </q-toolbar>
    </q-header>

    <q-page-container class="q-pt-xs">
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script>
import { defineComponent } from "vue";
import { useProductsStore } from "stores/products";
import { useUserStore } from "stores/users";

export default defineComponent({
  name: "MainLayout",
  setup() {
    const productStore = useProductsStore();
    const userStore = useUserStore();

    return {
      userStore,
      productStore,
      sortOptions: ["name", "price"],
    };
  },
});
</script>
