<template>
    <div class="send-panel">
        <div class="toolbar">
            <el-popover :visible="showEmojiPopover" trigger="click" placement="top" :teleported="false"
                @show="openPopover" @hide="closePopover" :popper-style="{ width: '490px', padding: '0 0px 10px 10px' }">
                <template #default>
                    <el-tabs v-model="activateEmoji">
                        <el-tab-pane v-for="(emoji, index) in emojiList" :key="index" :label="emoji.name"
                            :name="emoji.name">
                            <div class="emoji-list">
                                <div class="emoji-item" v-for="(item, index) in emoji.emojiList" :key="index"
                                    @click="sendEmoji(item)">
                                    {{ item }}
                                </div>
                            </div>
                        </el-tab-pane>
                    </el-tabs>
                </template>
                <template #reference>
                    <div class="iconfont icon-emoji" @click="showEmojiPopoverHandler"></div>
                </template>
            </el-popover>
            <el-upload ref="uploadRef" name="file" :show-file-list="false" :multiple="true" :limit="maxFileCount"
                :http-request="uploadFile" :on-exceed="uploadExceed">
                <div class="iconfont icon-folder"></div>
            </el-upload>
        </div>
        <div class="input-area" @drop="dropHandler" @dragover="dragoverHandle">
            <el-input :row="5" v-model="msgContent" type="textarea" resize="none" maxlength="500" show-word-limit
                spellcheck="false" input-style="background:#f5f5f5;border:none" @keydown.ctrl.enter="sendMsg"
                @paste="pasteFile" />
        </div>
        <div class="send-btn-panel">
            <el-popover :visible="showSendMsgPopover" trigger="manual" :hide-after="1500" placement="top-end"
                :teleported="false" @show="openMsgPopover" @hide="closeMsgPopover"
                :popper-style="{ width: '120px', padding: '5px', 'min-width': '0px' }">
                <template #default>
                    <span class="empty-msg">不能发送空白消息</span>
                </template>
                <template #reference>
                    <el-button class="send-btn" @click="sendMsg">发送</el-button>
                </template>
            </el-popover>
        </div>
        <SearchAdd ref="searchAddRef"></SearchAdd>
    </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, onMounted, onUnmounted, watch } from "vue"
const { proxy } = getCurrentInstance();
import emojiList from "@/utils/Emoji";

import { useUserInfoStore } from "@/stores/UserInfoStore";
const userInfoStore = useUserInfoStore();

import { useSysSettingStore } from "../../stores/SysSettingStore";
const sysSettingStore = useSysSettingStore();

import { getFileType } from "../../utils/Constants";
import SearchAdd from "../contact/SearchAdd.vue";

const activateEmoji = ref("常用")
const msgContent = ref('')
const searchAddRef = ref()

const props = defineProps({
    currentChatSession: {
        type: Object,
        default: () => ({})
    }
})

watch(() => props.currentChatSession,
    (newVal, oldVal) => {
        if (newVal) {
            msgContent.value = ""
        }
    },
    { immediate: true }
)

//隐藏展示pop
const showSendMsgPopover = ref(false)
const showEmojiPopover = ref(false)
const hidePopover = () => {
    showSendMsgPopover.value = false;
    showEmojiPopover.value = false;
}

const sendMsg = (e) => {
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

const emit = defineEmits(["sendMessage4Local"])

const sendMessageData = async (messageObj = {
    messageContent,
    messageType,
    localFilePath,
    fileSize,
    fileName,
    filePath,
    fileType
}, cleanMsgContent) => {
    if (!checkFile(messageObj.fileType, messageObj.fileSize, messageObj.fileName)) {
        return;
    }
    if (messageObj.fileSize === 0) {
        proxy.Confirm({
            message: `${messageObj.fileName}是一个空文件，请重新选择文件`,
            showCancelButton: false
        })
        return;
    }
    messageObj.sessionId = props.currentChatSession.sessionId;
    messageObj.sendUserId = userInfoStore.getInfo().userId;

    let result = await proxy.Request({
        url: proxy.Api.sendMessage,
        showLoading: false,
        method: 'post',
        params: {
            messageContent: messageObj.messageContent,
            contactId: props.currentChatSession.contactId,
            messageType: messageObj.messageType,
            fileSize: messageObj.fileSize,
            fileName: messageObj.fileName,
            fileType: messageObj.fileType
        },
        showError: false,
        errorCallback: (responseData) => {
            proxy.Confirm({
                message: responseData.info,
                okfun: () => {
                    searchAddRef.value.show({ contactId: props.currentChatSession.contactId })
                },
                okText: "确定"
            })
        }
    })
    if (!result) {
        return;
    }
    if (cleanMsgContent) {
        msgContent.value = ""
    }
    Object.assign(messageObj, result.data)
    emit("sendMessage4Local", messageObj)
    //保存消息至本地
    window.ipcRenderer.send("addLocalMessage", messageObj)
}

const uploadExceed = (files) => {
    checkFileCount(files)
}

const uploadRef = ref()

const uploadFile = (file) => {
    uploadFileDo(file.file);
    uploadRef.value.clearFiles()
}

const getFileTypeByName = (fileName) => {
    const suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
    return getFileType(suffix);
}

const uploadFileDo = (file) => {
    const fileType = getFileTypeByName(file.name);
    sendMessageData({
        messageContent: '[' + getFileType(fileType) + ']',
        messageType: 5,
        fileSize: file.size,
        fileName: file.name,
        filePath: file.path,
        fileType: fileType
    })
}

const showEmojiPopoverHandler = () => {
    showEmojiPopover.value = !showEmojiPopover.value
}

const openPopover = () => {
    showEmojiPopover.value = true;
    document.addEventListener("click", hidePopover, false);
}

const closePopover = () => {
    showEmojiPopover.value = false;
    document.removeEventListener("click", hidePopover, false);
}

const openMsgPopover = () => {
    showSendMsgPopover.value = true;
    document.addEventListener("click", hidePopover, false);
}

const closeMsgPopover = () => {
    showSendMsgPopover.value = false;
    document.removeEventListener("click", hidePopover, false);
}

const sendEmoji = (emoji) => {
    msgContent.value += emoji;
    showEmojiPopover.value = false;
}

//校验文件大小
const checkFile = (fileType, fileSize, fileName) => {
    const SIZE_MB = 1024 * 1024;
    const settingArray = Object.values(sysSettingStore.getSysSetting())
    const fileSizeMax = SIZE_MB * settingArray[fileType];
    if (fileSize > fileSizeMax) {
        proxy.Confirm({
            message: `${fileName}文件超过最大${settingArray[fileType]}MB限制，请重新选择文件`,
            showCancelButton: false
        })
        return false;
    }
    return true;
}

//发送文件数量
const maxFileCount = ref(5);
const checkFileCount = (files) => {
    console.log("files:", files);
    if (files.length > maxFileCount.value) {
        proxy.Confirm({
            message: `一次最多只能发送${maxFileCount.value}个文件，请重新选择文件`,
            showCancelButton: false
        })
        return false;
    }
    return true;
}

//拖入文件
const dragoverHandle = (e) => {
    e.preventDefault();

}

const dropHandler = (event) => {
    event.preventDefault();
    const files = event.dataTransfer.files;
    console.log("files:", files);
    if (!checkFileCount(files)) {
        return;
    }
    for (let i = 0; i < files.length; i++) {
        uploadFileDo(files[i]);
    }
}

const pasteFile = async (event) => {
    let items = event.clipboardData.items;
    const fileData = {};
    for (const item of items) {
        if (item.kind !== "file") {
            break;
        }
        const file = await item.getAsFile();
        if (file.path !== undefined && file.path !== "") {
            uploadFileDo(file);
        } else {
            const imageFile = new File([file], 'temp.jpg');
            let fileReader = new FileReader();
            fileReader.onloadend = function () {
                const byteArray = new Uint8Array(this.result);
                fileData.byteArray = byteArray;
                fileData.name = imageFile.name;
                window.ipcRenderer.send("saveClipBoardFile", fileData);
            }
            fileReader.readAsArrayBuffer(imageFile);
        }
    }
}

onMounted(() => {
    window.ipcRenderer.on("saveClipBoardFileCallBack", (event, file) => {
        const fileType = 0;
        sendMessageData({
            messageContent: '[' + getFileType(fileType) + ']',
            messageType: 5,
            fileSize: file.size,
            fileName: file.name,
            filePath: file.path,
            fileType: fileType
        }, false)
    })
})

onUnmounted(() => {
    window.ipcRenderer.removeAllListeners("saveClipBoardFileCallBack");
})

</script>

<style lang="scss" scoped>
.emoji-list {
    max-height: 260px; // 最大高度
    overflow-y: auto;
    flex-wrap: wrap;
    display: grid;
    grid-template-columns: repeat(10, 1fr);

    .emoji-item {
        display: flex;
        align-items: center;
        justify-content: center;
        height: 32px;
        width: 32px;
        font-size: 22px;
        cursor: pointer;

        &:hover {
            background: #ddd;
        }
    }
}

:deep(.el-tabs__nav-wrap::after) {
    height: 0;
}

.send-panel {
    height: 200px;
    border-top: 1px solid #ddd;

    .toolbar {
        height: 40px;
        display: flex;
        align-items: center;
        padding-left: 10px;

        .iconfont {
            color: #494949;
            font-size: 20px;
            margin-left: 10px;
            cursor: pointer;
        }

        :deep(.el-tabs__header) {
            margin-bottom: 0px;
        }

    }

    .input-area {
        padding: 0 10px;
        outline: none;
        width: 100%;
        height: 105px;
        overflow: auto;
        word-wrap: break-word;
        word-break: break-all;

        :deep(.el-textarea__inner) {
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
            color: #07c160;
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
