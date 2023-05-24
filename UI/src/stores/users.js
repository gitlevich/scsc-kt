import { ref } from "vue";
import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
    state: () => ({
        user: ref("Milen"),
        users: ["Milen", "Allard", "Sara"],
    }),
})