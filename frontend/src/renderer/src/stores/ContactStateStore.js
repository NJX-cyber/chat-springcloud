import { defineStore } from "pinia";
export const useContactStateStore = defineStore("contactState", {
    state: () => {
        return {
            contactReload: null
        };
    },
    actions: {
        setContactReload(state) {
            this.contactReload = state;
        }
    }

});