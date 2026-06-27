<template>
    <div>
        <AvatarBase :userId="userId" :width="width" :borderRadius="borderRadius" v-if="userId === 'Urobot'"
            :showDetail="false"></AvatarBase>
        <el-popover v-else :width="280" placement="right-start" :show-arrow="false" trigger="click" transition="none"
            :hide-after="0" @show="getContactInfo" ref="popoverRef">
            <template #reference>
                <AvatarBase :userId="userId" :width="width" :borderRadius="borderRadius" :showDetail="showDetail">
                </AvatarBase>
            </template>
            <template #default>
                <UserBaseInfo :userInfo="userInfo"></UserBaseInfo>
                <div class="op-btn" v-if="userId !== userInfoStore.getInfo().userId">
                    <el-button v-if="userInfo.contactStatus == 1" type="primary" @click="sendMessage()">发送消息</el-button>
                    <el-button v-else type="primary" @click="addContact">加为好友</el-button>
                </div>
            </template>
        </el-popover>
        <SearchAdd ref="searchAddRef" @reload="resetForm"></SearchAdd>
    </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
import AvatarBase from "../components/AvatarBase.vue"
import UserBaseInfo from "../components/UserBaseInfo.vue"
import SearchAdd from "@/views/contact/SearchAdd.vue";
import { useUserInfoStore } from "@/stores/UserInfoStore";
const userInfoStore = useUserInfoStore();
const { proxy } = getCurrentInstance();

import { useRouter, useRoute } from "vue-router"
const router = useRouter()
const route = useRoute()

const props = defineProps({
    userId: {
        type: String,
        default: ""
    },
    width: {
        type: Number,
        default: 40
    },
    borderRadius: {
        type: Number,
        default: 0
    },
    showDetail: {
        type: Boolean,
        default: false
    },
})

const userInfo = ref({})
const getContactInfo = async () => {
    userInfo.value.userId = props.userId
    if (userInfoStore.getInfo().userId === props.userId) {
        userInfo.value = userInfoStore.getInfo()
        return
    } else {
        let result = await proxy.Request({
            url: proxy.Api.getUserContactInfo,
            method: 'get',
            params: {
                contactId: props.userId
            },
            showLoading: false
        })
        if (!result) {
            return
        }
        userInfo.value = Object.assign({}, result.data)
    }
}

const emit = defineEmits(["closeDrawer"])

const popoverRef = ref()
const sendMessage = () => {
    popoverRef.value.hide();
    emit("closeDrawer");
    router.push({
        path: "/chat",
        query: {
            contactId: userInfo.value.userId,
            timestamp: Date.now()
        }
    })
}

const searchAddRef = ref()
const addContact = () => {
    popoverRef.value.hide();
    searchAddRef.value.show({
        contactId: props.userId,
        contactType: "USER",
    })
}

const resetForm = () => { 
    searchResult.value = {};
}

</script>

<style lang="scss" scoped>
.op-btn {
    border-radius: 5px;
    margin-top: 10px;
    padding: 10px;
    background: #fff;
    text-align: center;
}
</style>
