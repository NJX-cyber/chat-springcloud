import { defineStore } from "pinia"; 
export const useSysSettingStore = defineStore("sysSettingStore", {
    state: () => {
        return {
            sysSetting: {},
        };
    },
    actions: {
        setSysSetting(sysSetting) {
            this.sysSetting = sysSetting;
        },

        getSysSetting() {
            return this.sysSetting;
        },
    }

});