<template>
  <q-layout view="hHh lpR fFf">
    <q-header reveal elevated class="bg-accent text-white">
      <q-toolbar>
        <q-toolbar-title> Simple Payment Service </q-toolbar-title>
      </q-toolbar>
    </q-header>

    <q-page-container class="q-pt-xs">
      <q-card dark bordered class="bg-accent absolute-center">
        <q-card-section>
          <div class="text-h6">Payments for order</div>
          <div class="text-subtitle2">{{ orderId }}</div>
        </q-card-section>

        <q-separator dark inset />

        <q-card-section style="width: 500px">
          <q-list>
            <q-item>
              <q-item-section>
                <q-item-label class="text-h6">Order value</q-item-label>
              </q-item-section>
              <q-item-section side>
                <q-item-label class="text-white text-h6"
                  >{{ orderPayment.requestedAmount }} credits</q-item-label
                >
              </q-item-section>
            </q-item>
            <q-item>
              <q-item-section>
                <q-item-label class="text-h6">Paid</q-item-label>
              </q-item-section>
              <q-item-section side>
                <q-item-label class="text-white text-h6"
                  >{{ orderPayment.paidAmount }} credits</q-item-label
                >
              </q-item-section>
            </q-item>
            <q-item>
              <q-item-section>
                <q-item-label class="text-h6">Due</q-item-label>
              </q-item-section>
              <q-item-section side>
                <q-item-label class="text-white text-h6"
                  >{{ amountDue() }} credits</q-item-label
                >
              </q-item-section>
            </q-item>
          </q-list>

          <q-separator dark inset />
          <q-item v-if="amount >0" class="q-pa-lg q-mt-lg" style="background-color: white">
            <q-item-section>
              <q-input
                outline
                stack-label
                v-model="amount"
                mask="####.##"
                reverse-fill-mask
                label-color="accent"
                bg-color="white"
                color="accent"
                class="text-h6"
                input-style="text-align: right; padding-right: 2rem;"
              >
                <template v-slot:prepend> Credits </template>
              </q-input>
            </q-item-section>
            <q-item-section side>
              <q-btn
                class="full-width"
                icon="payment"
                label="Pay now"
                color="accent"
                @click="makePayment()"
              />
            </q-item-section>
          </q-item>
        </q-card-section>
      </q-card>
    </q-page-container>
  </q-layout>
</template>

<script>
import { defineComponent, ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import { commandGateway, queryGateway } from "boot/axios";
import useQuasar from "quasar/src/composables/use-quasar.js";

export default defineComponent({
  name: "IndexPage",
  components: {},
  setup() {
    const $q = useQuasar();
    const route = useRoute();
    const orderId = ref("");
    const orderPayment = ref({});
    const amount = ref(0);

    onMounted(() => {
      orderId.value = route.params.order;
      getPayment();
    });

    function formatPrices() {
      orderPayment.value.requestedAmount =
        orderPayment.value.requestedAmount.toFixed(2);
      orderPayment.value.paidAmount = orderPayment.value.paidAmount.toFixed(2);
    }

    function amountDue() {
      return (
        orderPayment.value.requestedAmount - orderPayment.value.paidAmount
      ).toFixed(2);
    }

    function getPayment() {
      queryGateway
        .post("", {
          name: "demo.scsc.api.payment.GetPaymentForOrderQuery",
          payload: {
            orderId: orderId.value,
          },
          payloadType: "demo.scsc.api.payment.GetPaymentForOrderQuery",
          responseType: "demo.scsc.api.payment.GetPaymentForOrderQueryResponse",
        })
        .then((response) => {
          console.log("response: " + response.data.payload);
          orderPayment.value = response.data.payload;
          formatPrices();
          amount.value = amountDue()
        })
        .catch((error) => {
          console.error(error);
          throw error;
        });
    }

    function makePayment() {
      commandGateway
        .post("", {
          name: "demo.scsc.api.payment.ProcessPaymentCommand",
          payload: {
            orderPaymentId: orderPayment.value.id,
            amount: amount.value,
          },
          payloadType: "demo.scsc.api.payment.ProcessPaymentCommand",
        })
        .then(() => {
          $q.notify({
            type: "positive",
            message: "Payment processed successfully",
          });
          getPayment();
        })
        .catch((error) => {
          $q.notify({
            type: "negative",
            message: "Error: " + error.response.data.error,
          });
        });
    }

    return {
      orderId: orderId,
      orderPayment,
      amount,
      amountDue,
      makePayment,
    };
  },
});
</script>
