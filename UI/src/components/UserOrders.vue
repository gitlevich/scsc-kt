<template>
  <div>
    <q-toolbar class="text-primary q-mb-md q-mt-xl">
      <q-toolbar-title class="text-h5"> Orders </q-toolbar-title>
      <q-toggle v-model="refresh" checked-icon="check" unchecked-icon="clear" />
    </q-toolbar>

    <h5 class="text-blue-grey-6 text-weight-bolder"></h5>
    <q-banner v-if="orderStore.orders.length == 0" class="bg-transparent">
      There are no orderers so far!
    </q-banner>
    <q-list separator>
      <q-expansion-item
        v-for="order in orderStore.orders"
        :key="order.id"
        group="orders"
      >
        <template v-slot:header>
          <q-item-section avatar>
            <q-icon v-if="order.isShipped" name="verified" color="positive">
              <q-tooltip> Order was shipped! </q-tooltip>
            </q-icon>
            <q-icon
              v-if="!order.isShipped"
              :name="order.isPaid ? 'credit_score' : 'payment'"
              :color="order.isPaid ? 'positive' : 'accent'"
            >
              <q-tooltip v-if="!order.isPaid"> Expecting payment! </q-tooltip>
              <q-tooltip v-if="order.isPaid"> Payment received! </q-tooltip>
            </q-icon>
            <q-icon
              v-if="!order.isShipped"
              :name="order.isPrepared ? 'inventory_2' : 'how_to_vote'"
              :color="order.isPrepared ? 'positive' : 'accent'"
            >
              <q-tooltip v-if="!order.isPrepared"> In preparation </q-tooltip>
              <q-tooltip v-if="order.isPrepared"> Package ready to ship! </q-tooltip>
            </q-icon>
          </q-item-section>
          <q-item-section>
            <q-item-label>Order</q-item-label>
            <q-item-label caption lines="2">{{ order.id }}</q-item-label>
          </q-item-section>
          <q-item-section side>
            <q-item-label class="text-weight-bolder">{{
              order.total
            }}</q-item-label>
          </q-item-section>
        </template>
        <q-list separator>
          <q-item v-for="line in order.lines" :key="line.id">
            <q-item-section>
              <q-item-label class="caption">{{ line.name }}</q-item-label>
            </q-item-section>
            <q-item-section side>
              <q-item-label>{{ line.price }}</q-item-label>
            </q-item-section>
          </q-item>
        </q-list>
        <div class="row">
          <q-btn
            v-if="!order.isShipped && !order.isPaid"
            class="full-width"
            icon="payment"
            label="Pay now"
            color="accent"
            :href="'#/payment/' + order.id"
            target="payment"
          />
        </div>
      </q-expansion-item>
    </q-list>
  </div>
</template>

<script>
import { defineComponent, ref, onMounted } from "vue";
import { useOrderStore } from "stores/orders";
import { useUserStore } from "stores/users";

export default defineComponent({
  name: "UserOrders",

  setup() {
    const orderStore = useOrderStore();
    const userStore = useUserStore();
    const pollInterval = ref({});
    const refresh = ref(true);

    // const paymentIcon = computed((orderLine) => {
    //   return {
    //     "icon": orderLine.isPaid ? "credit_score" : "payment",
    //     "color": orderLine.isPaid ? "positive" : "secondary",
    //     "caption" : orderLine.isPaid ? "Order is paid. Thank You!" : "Expecting payment!",
    //   };
    // });

    function getUserOrders() {
      if (refresh.value) {
        orderStore.getUserOrders(userStore.user);
      }
    }
    onMounted(() => {
      pollInterval.value = setInterval(getUserOrders, 1000); //save reference to the interval
      setTimeout(() => {
        clearInterval(pollInterval.value);
      }, 36000000); //stop polling after an hour
    });

    return {
      orderStore,
      refresh,
    };
  },
});
</script>
