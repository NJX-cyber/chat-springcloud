<template>
    <ContentPanel>
        <div class="search-form">
            <el-input placeholder="请输入用户ID或群组ID" v-model="contactId" size="large" @keydown.enter="search"></el-input>
            <div class="search-btn iconfont icon-search" @click="search"></div>
        </div>
        <div v-if="searchResult && Object.keys(searchResult).length > 0" class="search-result-panel">
            <div class="search-result">
                <span class="contact-type">{{ contactTypeName }}</span>
                <UserBaseInfo :userInfo="searchResult" :showArea="searchResult.contactType === 'USER'"></UserBaseInfo>
            </div>
            <div class="op-btn" v-if="searchResult.contactId != userInfoStore.getInfo().userId">
                <el-button type="primary"
                    v-if="searchResult.status == null || searchResult.status == 0 || searchResult.status == 2 || searchResult.status == 3 || searchResult.status == 4"
                    @click="applyContact">
                    {{ searchResult.contactType == 'USER' ? '加为好友' : '加入群组' }}
                </el-button>
                <el-button type="primary"
                    v-if="searchResult.status == 1"
                    @click="sendMessage">
                    发送消息
                </el-button>

            </div>
        </div>
        <div v-if="!searchResult || Object.keys(searchResult).length === 0" class="no-data">没有任何搜索结果</div>
    </ContentPanel>
    <SearchAdd ref="searchAddRef" @reload="resetForm"></SearchAdd>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, computed } from "vue"
const { proxy } = getCurrentInstance();

import { useUserInfoStore } from "@/stores/UserInfoStore";
import SearchAdd from "./SearchAdd.vue";
const userInfoStore = useUserInfoStore();

const contactTypeName = computed(() => {
    if (userInfoStore.getInfo().userId === searchResult.value.contactId) {
        return '自己';
    }
    if (searchResult.value.contactType === 'USER') {
        return '用户';
    } else {
        return '群组';
    }
});

const contactId = ref("");

const searchResult = ref({});

const search = async () => {
    if (!contactId.value) {
        proxy.Message.warning("请输入用户ID或群组ID");
        return;
    }
    let result = await proxy.Request({
        url: proxy.Api.search,
        method: 'get',
        params: {
            contactId: contactId.value
        }
    })
    
    if (!result) {
        searchResult.value = {};
        return;
    }
    searchResult.value = result.data;
}

const resetForm = () => { 
    searchResult.value = {};
}

const searchAddRef = ref();
const applyContact = async () => { 
    searchAddRef.value.show(searchResult.value)
    contactId.value = undefined;
}

</script>

<style lang="scss" scoped>
.search-form {
    padding-top: 50px;
    display: flex;
    align-items: center;

    :deep(.el-input__wrapper) {
        border-radius: 4px 0 0 4px;
        border-right: none;
    }

    .search-btn {
        background: #6366f1;
        color: #fff;
        line-height: 40px;
        width: 80px;
        text-align: center;
        border-radius: 0 5px 5px 0;
        cursor: pointer;

        &:hover {
            background: #0dd36c;
        }
    }
}

.no-data {
    padding: 30px 0px;
}

.search-result-panel {
    .search-result {
        padding: 30px 20px 20px 20px;
        background: #fff;
        border-radius: 5px;
        margin-top: 10px;
        position: relative;

        .contact-type {
            position: absolute;
            left: 0px;
            top: 0px;
            background: #2cb6fe;
            padding: 2px 5px;
            color: #fff;
            border-radius: 5px 0 0 5px;
            font-size: 10px;
        }
    }

    .op-btn {
        border-radius: 5px;
        margin-top: 10px;
        padding: 10px;
        background: #fff;
        text-align: center;
    }
}
</style>
