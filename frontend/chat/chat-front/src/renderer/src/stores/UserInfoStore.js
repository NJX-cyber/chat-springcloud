import { defineStore } from "pinia";
export const useUserInfoStore = defineStore("userinfo", {
  state: () => {
    return {
      userinfo: {},
    };
  },
  actions: {
    setUserInfo(userinfo) {
      this.userinfo = userinfo;
      localStorage.setItem("userInfo", JSON.stringify(userinfo));
    },

    getInfo() {
      if (!this.userinfo || Object.keys(this.userinfo).length === 0) {
          try {
              const raw = localStorage.getItem("userInfo");
              this.userinfo = raw ? JSON.parse(raw) : null;
          } catch {
              this.userinfo = null;
          }
      }
      return this.userinfo;
    }
  },

});