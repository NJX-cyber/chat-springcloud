import { defineStore } from "pinia";
export const useMessageCountStore = defineStore("messageCount", {
    state: () => {
        return {
            messageCount: {
                chatCount: 0,
                contactApplyCount: 0,
            },
        };
    },
    actions: {
        setCount(key, count, forceUpdate) {
            if (forceUpdate) {
                this.messageCount[key] = count;
                return;
            }
            let oldCount = this.messageCount[key];
            let newCount = oldCount + count;
            this.messageCount[key] = newCount < 0 ? 0 : newCount;
        },
        getCount(key) {
            return this.messageCount[key];
        },
    },
});