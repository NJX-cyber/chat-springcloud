<template>
    <ContentPanel :showTopBorder="true" :infinite-scroll-immediate="false" v-infinite-scroll="loadApply">
        <div>
            <div class="apply-item" v-for="item in applyList" :key="item.applyId">
                <div :class="['contact-type', item.contactType == 0 ? 'user-contact' : '']">
                    {{ item.contactType === 0 ? '好友' : '群聊' }}
                </div>
                <Avatar :width="50" :userId="item.applyUserId"></Avatar>
                <div class="contact-info">
                    <div class="nick-name">{{ item.contactName }}</div>
                    <div class="apply-info">{{ item.applyInfo }}</div>
                </div>
                <div class="op-btn">
                    <div v-if="item.status === 0">
                        <el-dropdown placement="bottom-end" trigger="click">
                            <span class="el-dropdown-link">
                                <el-button type="text" size="small">{{ item.statusName }}</el-button>
                            </span>
                            <template #dropdown>
                                <el-dropdown-item
                                    @click="dealWithApply(item.applyId, item.contactType, 1)">同意</el-dropdown-item>
                                <el-dropdown-item
                                    @click="dealWithApply(item.applyId, item.contactType, 2)">拒绝</el-dropdown-item>
                                <el-dropdown-item
                                    @click="dealWithApply(item.applyId, item.contactType, 3)">拉黑</el-dropdown-item>
                            </template>
                        </el-dropdown>
                    </div>
                    <div v-else class="result-name">{{ item.statusName }}</div>
                </div>
            </div>
        </div>
        <div v-if="applyList.length === 0" class="no-data">暂无申请</div>
    </ContentPanel>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, onMounted, watch } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router";
const route = useRoute();
const router = useRouter();

import { useContactStateStore } from "@/stores/ContactStateStore";
const contactStateStore = useContactStateStore();

import { useMessageCountStore } from "@/stores/MessageCountStore";
const messageCountStore = useMessageCountStore();

const applyList = ref([])

let pageNum = ref(0);
let pageTotal = ref(1);

const loadApply = async () => {
    pageNum.value = pageNum.value + 1;
    if (pageNum.value > pageTotal.value) {
        return;
    }
    let result = await proxy.Request({
        url: proxy.Api.loadApply,
        method: "get",
        params: {}
    })
    if (!result) {
        return;
    }
    pageTotal.value = result.data.pageTotal;
    if (pageNum.value === 1) {
        applyList.value = [];
    }
    applyList.value = applyList.value.concat(result.data.list);
    pageNum.value = result.data.pageNum;
}

const dealWithApply = (applyId, contactType, result) => {
    proxy.Confirm({
        title: "提示",
        message: "确定要" + (result === 1 ? "同意" : result === 2 ? "拒绝" : "拉黑") + "该申请吗？",
        okfun: async () => {
            let res = await proxy.Request({
                url: proxy.Api.dealWithApply,
                method: "put",
                params: {
                    applyId,
                    status: result
                }
            })
            if (!res) {
                return;
            }
            proxy.Message.success("操作成功");
            // 处理完后刷新列表
            pageNum.value = 0;
            loadApply();

            //刷新联系人列表
            contactStateStore.setContactReload(null);
            if (contactType === 0 && result === 1) { // 同意好友申请才刷新好友列表
                contactStateStore.setContactReload("USER");
            } else if (contactType === 1 && result === 1) { // 同意群聊申请才刷新群聊列表 
                contactStateStore.setContactReload("GROUP");
            }

        }
    })
}

watch(() => messageCountStore.messageCount.contactApplyCount, (newValue, oldValue) => {
    if (newValue) {
        pageNum.value = 1;
        loadApply();
    }
})

onMounted(() => {
    loadApply();
});


</script>

<style lang="scss" scoped>
.apply-item {
    display: flex;
    align-items: center;
    border-bottom: 1px solid #ddd;
    padding: 10px 0px;

    .contact-type {
        display: flex;
        justify-content: center;
        writing-mode: vertical-rl;
        vertical-align: middle;
        background: #2cb6fe;
        color: #fff;
        border-radius: 5px 0px 0px 5px;
        height: 50px;
    }

    .user-contact {
        background: #08bfb1;
    }

    .contact-info {
        width: 260px;
        margin-left: 10px;

        .nick-name {
            color: #000000;
        }

        .apply-info {
            color: #999999;
            font-size: 12px;
            margin-top: 5px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
    }

    .op-btn {
        width: 50px;
        text-align: center;

        .result-name {
            color: #999999;
            font-size: 12px;
        }
    }
}
</style>
