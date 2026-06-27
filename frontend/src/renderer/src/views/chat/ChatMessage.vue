<template>
    <div class="message-content-my" v-if="data.sendUserId === userInfoStore.getInfo()?.userId">
        <div :class="['content-panel', data.messageType === 5 ? 'content-panel-media' : '']">
            <div class="sending" v-if="data.status === 0">
                <el-skeleton :animated="true">
                    <template #template>
                        <el-skeleton-item class="skeleton-item" variant="image"></el-skeleton-item>
                    </template>
                </el-skeleton>
            </div>
            <template v-else>
                <div class="content" v-html="data.messageContent" v-if="data.messageType !== 5"></div>
                <div class="content" v-else>
                    <template v-if="data.fileType === 0">
                        <ChatMessageImage :data="data" @click="showDetail"></ChatMessageImage>
                    </template>
                    <template v-if="data.fileType === 1">
                        <ChatMessageVideo :data="data" @click="showDetail"></ChatMessageVideo>
                    </template>
                    <template v-if="data.fileType === 2">
                        <ChatMessageFile :data="data" @click="showDetail"></ChatMessageFile>
                    </template>
                </div>
            </template>
        </div>
        <div class="user-avatar">
            <Avatar :width="35" :userId="userInfoStore.getInfo()?.userId"></Avatar>
        </div>
    </div>
    <div class="message-content-other" v-else>
        <div class="user-avatar">
            <Avatar :width="35" :userId="data.sendUserId"></Avatar>
        </div>
        <div
            :class="['content-panel', data.contentType === 1 ? 'group-content' : '', data.messageType === 5 ? 'content-panel-media' : '']">
            <div class="nick-name" v-if="data.contentType === 1">{{ data.sendUserName }}</div>
            <div class="sending" v-if="data.status === 0">
                <el-skeleton :animated="true">
                    <template #template>
                        <el-skeleton-item class="skeleton-item" variant="image"></el-skeleton-item>
                    </template>
                </el-skeleton>
            </div>
            <!-- AI 思考中 -->
            <div class="ai-thinking" v-if="data.isThinking">
                <span class="dot"></span><span class="dot"></span><span class="dot"></span>
            </div>
            <template v-else>
                <div class="content" v-html="renderedContent" v-if="data.messageType !== 5"></div>
                <div class="content" v-else>
                    <template v-if="data.fileType === 0">
                        <ChatMessageImage :data="data" @click="showDetail"></ChatMessageImage>
                    </template>
                    <template v-if="data.fileType === 1">
                        <ChatMessageVideo :data="data" @click="showDetail"></ChatMessageVideo>
                    </template>
                    <template v-if="data.fileType === 2">
                        <ChatMessageFile :data="data" @click="showDetail"></ChatMessageFile>
                    </template>
                </div>
            </template>
        </div>
    </div>
</template>

<script setup>
import ChatMessageImage from "./ChatMessageImage.vue";
import ChatMessageVideo from "./ChatMessageVideo.vue";
import ChatMessageFile from "./ChatMessageFile.vue";
import { ref, reactive, getCurrentInstance, nextTick, computed } from "vue"
const { proxy } = getCurrentInstance();
import { useUserInfoStore } from "@/stores/UserInfoStore";
const userInfoStore = useUserInfoStore();

import { marked } from "marked";

const props = defineProps({
    data: {
        type: Object,
        default: () => ({})
    },
    currentChatSession: {
        type: Object,
        default: () => ({})
    },

})

const renderedContent = computed(() => {
    if (props.data.messageType !== 5 && props.data.messageContent && props.data.sendUserId !== userInfoStore.getInfo()?.userId) {
        return marked(props.data.messageContent)  // 转成 HTML
    }
    return ''
})

const emit = defineEmits(["showMediaDetail"]);

const showDetail = () => {
    if (props.data.status === 0) {
        return;
    }
    emit("showMediaDetail", props.data.messageId);
}

</script>

<style lang="scss" scoped>
.sending {
    width: 170px;
    height: 170px;
    overflow: hidden;
    float: right;
    margin-right: 5px;
    border-radius: 5px;

    .skeleton-item {
        width: 170px;
        height: 170px;
    }
}

.content {
    display: inline-block;
    padding: 8px;
    color: #474747;
    border-radius: 5px;
    text-align: left;
    font-size: 14px;

    :deep(.emoji) {
        font-size: 20px;
    }
}

.content-panel {
    flex: 1;
    position: relative;
    z-index: 1;

    &::after {
        content: '';
        position: absolute;
        display: block;
        width: 10px;
        height: 10px;
        background: #95ec69;
        transform: rotate(45deg);
        border-radius: 2px;
        top: 13px;
        z-index: 0;
    }
}

.content-panel-media {
    .content {
        border-radius: 5px;
        background: none !important;
        overflow: hidden;
        padding: 0;
    }

    &::after {
        display: none;
    }
}

.message-content-my {
    display: flex;

    .content-panel {
        margin-right: 10px;
        text-align: right;
        padding-left: 32%;

        .content {
            background: #95ec69;
        }

        &::after {
            right: -4px;
        }
    }
}

.message-content-other {
    display: flex;
    padding-right: 32%;

    .user-avatar {
        margin-right: 10px;
        width: 30px;
        height: 30px;
    }

    .content-panel {
        flex: 1;
        position: relative;
        text-align: left;

        .nick-name {
            font-size: 12px;
            color: #b2b2b2;
        }

        .content {
            background: #fff;

            :deep(p) {
                margin: 0;
            }
        }

        .sending {
            float: left;
        }

        &::after {
            left: -2px;
            background: #fff;
        }
    }

    .content-panel-media {
        justify-content: flex-start;
    }
}

.group-content {
    margin-top: -6px;

    .content {
        margin-top: 6px;
    }

    &::after {
        left: -4px;
        top: 35px;
        background: #fff;
    }
}

.ai-thinking {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 12px 18px;
    background: #fff;
    border-radius: 5px;

    .dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: #b0b0b0;
        animation: bounce 1.4s infinite ease-in-out both;

        &:nth-child(1) { animation-delay: -0.32s; }
        &:nth-child(2) { animation-delay: -0.16s; }
        &:nth-child(3) { animation-delay: 0s; }
    }
}

@keyframes bounce {
    0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
    40% { transform: scale(1); opacity: 1; }
}
</style>
