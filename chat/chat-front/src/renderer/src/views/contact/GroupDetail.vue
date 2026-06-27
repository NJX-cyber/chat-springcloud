<template>
    <ContentPanel>
        <div class="group-info-item">
            <div class="group-title">群封面：</div>
            <div class="group-value">
                <Avatar :userId="groupInfo.groupId"></Avatar>
            </div>
            <el-dropdown placement="bottom-end" trigger="click">
                <span class="el-dropdown-link">
                    <div class="iconfont icon-more"></div>
                </span>
                <template #dropdown>
                    <el-dropdown-menu v-if="groupInfo.groupOwnerId === userInfoStore.getInfo().userId">
                        <el-dropdown-item @click="editGroupInfo">修改群信息</el-dropdown-item>
                        <el-dropdown-item @click="dissolveGroup">解散该群</el-dropdown-item>
                    </el-dropdown-menu>
                    <el-dropdown-menu v-else>
                        <el-dropdown-item @click="leaveGroup">退出该群</el-dropdown-item>
                    </el-dropdown-menu>
                </template>
            </el-dropdown>
        </div>
        <div class="group-info-item">
            <div class="group-title">群ID：</div>
            <div class="group-value">{{ groupInfo.groupId }}</div>
        </div>
        <div class="group-info-item">
            <div class="group-title">群名称：</div>
            <div class="group-value">{{ groupInfo.groupName }}</div>
        </div>
        <div class="group-info-item">
            <div class="group-title">群成员：</div>
            <div class="group-value">{{ groupInfo.memberCount }}</div>
        </div>
        <div class="group-info-item">
            <div class="group-title">加入权限：</div>
            <div class="group-value">{{ groupInfo.joinType == 0 ? '允许任何人加入' : '需要管理员邀请' }}</div>
        </div>
        <div class="group-info-item">
            <div class="group-title">公告：</div>
            <div class="group-value">{{ groupInfo.groupNotice || '-' }}</div>
        </div>
        <div class="group-info-item">
            <div class="group-title"></div>
            <div class="group-value">
                <el-button type="primary" @click="sendMessage">发送群消息</el-button>
            </div>
        </div>
    </ContentPanel>
    <GroupEditDialog ref="groupEditDialogRef" @reloadGroupInfo="getGroupInfo"></GroupEditDialog>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, watch } from "vue"
const { proxy } = getCurrentInstance();
import { useRouter, useRoute } from "vue-router";
const router = useRouter();
const route = useRoute();

import { useUserInfoStore } from "@/stores/UserInfoStore";
const userInfoStore = useUserInfoStore();

import { useContactStateStore } from "@/stores/ContactStateStore";
import GroupEditDialog from "./GroupEditDialog.vue";
const contactStateStore = useContactStateStore();

const groupInfo = ref({});
const groupId = ref();
const groupEditDialogRef = ref();

const getGroupInfo = async () => {
    let result = await proxy.Request({
        url: proxy.Api.getGroupInfo,
        method: 'get',
        params: {
            groupId: groupId.value
        }
    })
    if (!result) {
        return;
    }
    groupInfo.value = result.data;
}

const editGroupInfo = () => {
    groupEditDialogRef.value.show(groupInfo.value);
}

const dissolveGroup = () => { 
    proxy.Confirm({
        message: "确定要解散该群吗？",
        okfun: async () => {
            contactStateStore.setContactReload(null);
            let result = await proxy.Request({
                url: proxy.Api.dissolutionGroup,
                method: 'put',
                params: {
                    groupId: groupId.value
                }
            })
            if (!result) {
                return;
            }
            proxy.Message.success("解散成功");
            contactStateStore.setContactReload("DISSOLUTION_GROUP");
        }
    })
}

const leaveGroup = () => { 
    proxy.Confirm({
        message: "确定要退出该群吗？",
        okfun: async () => {
            contactStateStore.setContactReload(null);
            let result = await proxy.Request({
                url: proxy.Api.leaveGroup,
                method: 'post',
                params: {
                    groupId: groupId.value
                }
            })
            if (!result) {
                return;
            }
            proxy.Message.success("退出成功");
            contactStateStore.setContactReload("LEAVE_GROUP");
        }
    })
}

const sendMessage = () => {
    console.log("sendMessage:",groupInfo.value);
    router.push({
        path: "/chat",
        query: {
            contactId: groupInfo.value.groupId,
            timestamp: Date.now()
        }
    })
}

watch(
    () => route.query.contactId,
    (newVal, oldVal) => {
        if (newVal) {
            groupId.value = newVal;
            getGroupInfo()
        }
    },
    { immediate: true, deep: true }
)

</script>

<style lang="scss" scoped>

.group-info-item {
    display: flex;
    margin: 15px 0;
    align-items: center;
    .group-title {
        width: 100px;
        text-align: right;
    }
    .group-value {
        flex: 1;
    }
}
.notice {
    align-items: flex-start;
}
</style>
