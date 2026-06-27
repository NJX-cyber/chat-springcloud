import { defineStore } from "pinia"; 
export const useGlobalInfoStore = defineStore("globalInfo", {
    state: () => {
        return {
            globalInfo: {},
        };
    },
    actions: {
        setGlobalInfo(key, value) {
            this.globalInfo[key] = value;
        },
        getGlobalInfo() {
            return this.globalInfo;
        }
    }

});