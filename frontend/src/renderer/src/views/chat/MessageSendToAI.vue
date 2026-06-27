<template>
    <div class="send-panel">
        <div class="input-area">
            <el-input :row="5" v-model="msgContent" type="textarea" resize="none" maxlength="500" show-word-limit
                spellcheck="false" input-style="background:#f5f5f5;border:none" @keydown.ctrl.enter="sendMsg" />
        </div>
        <div class="send-btn-panel">
            <el-popover :visible="showSendMsgPopover" trigger="manual" :hide-after="1500" placement="top-end"
                :teleported="false" @show="openMsgPopover" @hide="closeMsgPopover"
                :popper-style="{ width: '120px', padding: '5px', 'min-width': '0px' }">
                <template #default>
                    <span class="empty-msg">不能发送空白消息</span>
                </template>
                <template #reference>
                    <el-button class="send-btn" :disabled="props.isAIReplying" @click="sendMsg">
                        {{ props.isAIReplying ? '回复中...' : '发送' }}
                    </el-button>
                </template>
            </el-popover>
        </div>
        <SearchAdd ref="searchAddRef"></SearchAdd>
    </div>
</template>

<script setup>
import { ref, getCurrentInstance, onMounted, onUnmounted, watch, reactive } from "vue"
import { useUserInfoStore } from "@/stores/UserInfoStore";
import SearchAdd from "../contact/SearchAdd.vue";
import { fetchEventSource } from '@microsoft/fetch-event-source';

const { proxy } = getCurrentInstance();
const userInfoStore = useUserInfoStore();

const msgContent = ref('')
const searchAddRef = ref()

const controller = new AbortController();

const props = defineProps({
    currentChatSession: {
        type: Object,
        default: () => ({})
    },
    isAIReplying: {
        type: Boolean,
        default: false
    }
})

const emit = defineEmits(["createAiMessage", "sendMessage4Local", "msgStreaming", "aiReplyStart", "aiReplyEnd"]);

// 监听当前会话切换，清空输入框（不重置回复状态，由父组件管理）
watch(() => props.currentChatSession,
    (newVal) => {
        if (newVal) {
            msgContent.value = ""
        }
    },
    { immediate: true }
)

// 发送空白消息提示框控制
const showSendMsgPopover = ref(false)
const hidePopover = () => {
    showSendMsgPopover.value = false;
}
const openMsgPopover = () => {
    showSendMsgPopover.value = true;
    document.addEventListener("click", hidePopover, false);
}
const closeMsgPopover = () => {
    showSendMsgPopover.value = false;
    document.removeEventListener("click", hidePopover, false);
}

// 触发发送文本消息
const sendMsg = () => {
    if (props.isAIReplying) return;
    const messageContent = msgContent.value ? msgContent.value.trim() : "";
    if (messageContent === "") {
        showSendMsgPopover.value = true;
        return;
    }
    sendMessageData({
        messageContent,
        messageType: 2,
    }, true)
}

const sendMessageData = async ({ messageContent, messageType }, cleanMsgContent = false) => {

    if (cleanMsgContent) {
        msgContent.value = "";
    }

    const useMessageObj = {
        messageId: Date.now(),
        sessionId: props.currentChatSession.sessionId,
        sendUserId: userInfoStore.getInfo().userId,
        messageContent: messageContent,
        messageType: 2,
        lastMessage: messageContent,
        sendTime: Date.now()
    }

    // 用户消息立刻显示
    emit("sendMessage4Local", useMessageObj);

    const aiMessageObj = reactive({
        messageId: Date.now() + 1,
        sessionId: props.currentChatSession.sessionId,
        sendUserId: 'Urobot',
        messageContent: "",
        messageType: 2,
        isThinking: true
    });
    // AI 气泡立刻出现
    emit("createAiMessage", aiMessageObj);

    let replyTimeout = null;
    const baseUrl = import.meta.env.PROD ? proxy.Api.prodDomain : "";

    replyTimeout = setTimeout(() => {
        emit("aiReplyEnd");
    }, 30000);

    fetchEventSource(baseUrl + proxy.Api.sendMessageToAI, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'token': localStorage.getItem("token") || "",
        },
        body: JSON.stringify({
            memoryId: userInfoStore.getInfo().userId,
            question: messageContent,
        }),
        signal: controller.signal,
        onopen(res) {
            emit("aiReplyStart");
            console.log("SSE连接成功", res);
        },

        onmessage(msg) {
            aiMessageObj.isThinking = false;
            const marker = "\n__FINAL__";
            if (msg.data.startsWith(marker)) {
                aiMessageObj.messageContent = msg.data.substring(marker.length);
            } else if (msg.data.indexOf(marker) > 0) {
                aiMessageObj.messageContent = msg.data.substring(msg.data.indexOf(marker) + marker.length);
            } else {
                aiMessageObj.messageContent += msg.data;
            }
            emit("msgStreaming");
        },
        onclose() {
            clearTimeout(replyTimeout);
            aiMessageObj.isThinking = false;
            emit("aiReplyEnd");
        },
        onerror(err) {
            clearTimeout(replyTimeout);
            emit("aiReplyEnd");
            proxy.Message.error("网络出现波动，请重试");
            console.error("连接出错", err);
            throw err;
        }
    });
}

// 组件卸载时取消进行中的 SSE 请求
onUnmounted(() => {
    controller.abort();
});

</script>

<style lang="scss" scoped>
.send-panel {
    height: 200px;
    border-top: 1px solid #ddd;

    .input-area {
        padding: 0 10px;
        outline: none;
        width: 100%;
        height: 105px;
        overflow: auto;
        word-wrap: break-word;
        word-break: break-all;

        :deep(.el-textarea),
        :deep(.el-textarea__inner) {
            padding-top: 5px;
            box-shadow: none;
            height: 100%;
        }

        :deep(.el-input__count) {
            background: none;
            right: 12px;
        }
    }

    .send-btn-panel {
        text-align: right;
        padding-top: 10px;
        margin-right: 22px;

        .send-btn {
            cursor: pointer;
            color: #6366f1;
            background: #e9e9e9;
            border-radius: 5px;
            padding: 14px 29px;

            &:hover {
                background: #d2d2d2;
            }
        }
    }

    .empty-msg {
        font-size: 13px;
    }
}
</style>