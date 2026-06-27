<template>
  <Layout>
    <template #left-content>
      <div class="drag-panel drag"></div>
      <div class="top-search">
        <el-input clearable placeholder="搜索" v-model="searchValue" size="small" @keyup="search">
          <template #suffix>
            <span class="iconfont icon-search"></span>
          </template>
        </el-input>
      </div>
      <div class="chat-session-list" v-show="!searchValue">
        <template v-for="item in chatSessionList">
          <ChatSession v-if="item !== null && item?.sessionId !== undefined" :data="item" :key="item.sessionId"
            @click="chatSessionClickHandler(item)" @contextmenu.prevent.stop="onContextmenu(item, $event)"
            :currentSession="item?.contactId === currentChatSession.contactId">
          </ChatSession>
        </template>
      </div>
      <div class="search-list" v-show="searchValue">
        <SearchResult :data="item" v-for="item in searchList" :key="item.sessionId" @click="searchClickHandler(item)">
        </SearchResult>
      </div>
    </template>
    <template #right-content>
      <div class="title-panel drag">
        <div class="title">
          <span>{{ currentChatSession.contactName }}</span>
          <span v-if="currentChatSession.contactType === 1">({{ currentChatSession.memberCount }})</span>
        </div>
      </div>
      <div v-if="currentChatSession.contactType === 1" class="iconfont icon-more no-drag" @click="showChatGroupDetail">
      </div>
      <div class="chat-panel" v-show="Object.keys(currentChatSession).length > 0">
        <div class="message-panel" id="message-panel">
          <div class="message-item" v-for="(data, index) in messageList" :key="data.messageId"
            :id="'message' + data.messageId">
            <!-- 消息时间 -->
            <template
              v-if="index > 1 && data.sendTime - messageList[index - 1].sendTime >= 300000 && (data.messageType === 2 || data.messageType === 5)">
              <ChatMessageTime :data="data"></ChatMessageTime>
            </template>
            <!-- 系统消息 -->
            <template v-if="[1, 3, 8, 9, 11, 12].includes(data.messageType)">
              <ChatMessageSys :data="data"></ChatMessageSys>
            </template>
            <ChatMessage v-if="[1, 2, 5].includes(data.messageType)" :key="data.messageId" :data="data"
              :currentChatSession="currentChatSession" @showMediaDetail="showMediaHandler" />
          </div>
        </div>
        <MessageSendToAI v-if="currentChatSession.contactId === AI_CONTACT_ID" :currentChatSession="currentChatSession"
          :isAIReplying="isAIReplying" @createAiMessage="handleCreateAiMessage" @sendMessage4Local="sendMessage4Local" @msgStreaming="goToBottom()"
          @aiReplyStart="isAIReplying = true" @aiReplyEnd="handleAiReplyEnd">
        </MessageSendToAI>
        <MessageSend :currentChatSession="currentChatSession" v-else @sendMessage4Local="sendMessage4Local">
        </MessageSend>
      </div>
      <div class="chat-blank" v-show="Object.keys(currentChatSession).length === 0">
        <Blank></Blank>
      </div>
    </template>
  </Layout>
  <ChatGroupDetail v-if="currentChatSession.contactType === 1" ref="chatGroupDetailRef"
    @delChatSessionCallBack="delChatSession"></ChatGroupDetail>
</template>

<script setup>
import ChatMessageTime from "./ChatMessageTime.vue";
import ChatMessageSys from "./ChatMessageSys.vue";
import Blank from "@/components/Blank.vue";
import ContextMenu from "@imengyu/vue3-context-menu";
import "@imengyu/vue3-context-menu/lib/vue3-context-menu.css";
import MessageSend from "./MessageSend.vue";
import MessageSendToAI from "./MessageSendToAI.vue";
import ChatMessage from "./ChatMessage.vue";
import ChatSession from "./ChatSession.vue";
import ChatGroupDetail from "./ChatGroupDetail.vue";
import SearchResult from "./SearchResult.vue";

defineOptions({
  name: "chat"
})

import { ref, getCurrentInstance, nextTick, onMounted, onUnmounted, watch, onActivated, onDeactivated } from "vue";
const { proxy } = getCurrentInstance();

import { useMessageCountStore } from "@/stores/messageCountStore";
const messageCountStore = useMessageCountStore();

import { useUserInfoStore } from "@/stores/UserInfoStore";
const userInfoStore = useUserInfoStore();

import { useRouter, useRoute } from "vue-router";
const router = useRouter();
const route = useRoute();

const chatSessionList = ref([]);
const currentChatSession = ref({});
const messageList = ref([]);
const isAIReplying = ref(false);
const hasGeneratedTitle = ref(false);

const messageCountInfo = {
  pageTotal: 0,
  pageNum: 0,
  maxMessageId: null,
  noData: false
};

const AI_CONTACT_ID = ref("Urobot");

const loadChatSession = () => {
  window.ipcRenderer.send("loadSessionData");
};

const sortChatSessionList = (dataList) => {
  dataList.sort((a, b) => {
    if (a.topType === b.topType) {
      return b.lastReceiveTime - a.lastReceiveTime;
    }
    return b.topType - a.topType;
  });
};

const setTop = (data) => {
  data.topType = data.topType === 0 ? 1 : 0;
  sortChatSessionList(chatSessionList.value);
  window.ipcRenderer.send("topChatSession", { contactId: data.contactId, topType: data.topType });
};


const delChatSessionList = (contactId) => {
  setTimeout(() => {
    chatSessionList.value = chatSessionList.value.filter(item => item.contactId !== contactId);
  }, 100);
}
const delChatSession = (contactId) => {
  delChatSessionList(contactId);
  setSessionSelect({});
  currentChatSession.value = {};
  window.ipcRenderer.send("delChatSession", { contactId });
};

const onContextmenu = (data, event) => {
  ContextMenu.showContextMenu({
    x: event.x,
    y: event.y,
    items: [
      {
        label: data.topType === 0 ? "置顶" : "取消置顶",
        onClick: () => {
          setTop(data);
        }
      },
      {
        label: "删除聊天",
        onClick: () => {
          proxy.Confirm({
            message: "确定要删除此聊天吗？",
            okfun: async () => {
              delChatSession(data.contactId);
            }
          });
        }
      }
    ]
  });
};

const showMediaHandler = (messageId) => {
  const data = messageList.value
    .filter(item => item.messageType === 5)
    .map(item => {
      return {
        partType: "chat",
        fileType: item.fileType,
        fileId: item.messageId,
        fileName: item.fileName,
        fileSize: item.fileSize,
        forceGet: false
      }
    });
  window.ipcRenderer.send("newWindow", { windowId: "media", title: "媒体信息", path: '/showMedia', data: { currentFileId: messageId, fileList: data } });
};

let distanceBottom = 0;
const chatSessionClickHandler = async (item) => {
  distanceBottom = 0;
  currentChatSession.value = Object.assign({}, item);

  messageCountStore.setCount("chatCount", -item.noReadCount, false);
  item.noReadCount = 0;
  messageList.value = [];
  messageCountInfo.pageTotal = 1;
  messageCountInfo.pageNum = 0;
  messageCountInfo.maxMessageId = null;
  messageCountInfo.noData = false;
  if (currentChatSession.value.contactId === AI_CONTACT_ID.value) {
    await getAiMessageList();
    goToBottom();
  } else {
    loadChatMessage();
  }
  setSessionSelect({ contactId: item.contactId, sessionId: item.sessionId });
};

const greetingText = `你好，我是小智！我是你的专属AI工作助手。\n\n我专注于为你提供以下服务，你可以直接向我下达指令：\n
  - 应用功能引导：带你了解并快速上手本应用的各项功能。\n- 办公与文案：润色邮件、提炼会议纪要、生成营销文案。\n- 聊天与提效：提取待办事项。
  \n\n请问今天有什么我可以帮你的？`;
const getAiMessageList = async () => {
  let result = await proxy.Request({
    url: proxy.Api.getMessageList,
    showLoading: false,
    method: "get",
    params: {
      memoryId: userInfoStore.getInfo().userId,
    }
  })
  if (!result) {
    return;
  }
  let index = 0;
  if (result.data.length > 0) {
    messageList.value.push({
      messageId: index++,
      sessionId: currentChatSession.value.sessionId,
      sendUserId: 'Urobot',
      messageContent: greetingText,
      messageType: 2
    });
  }
  result.data.forEach(item => (
    messageList.value.push({
      ...item,
      messageType: 2,
      messageId: index++,
    }))
  );
  // console.log("AI消息列表：", messageList.value);
  // console.log("AI消息列表：", messageList.value.length);
  // 首次进入触发自动流式自我介绍
  if (messageList.value.length === 0) {
    generateAiGreeting();
  }
}

let aiGreetingTimer = null;

// 新增流式生成问候语的方法
const generateAiGreeting = () => {
  // 清理之前的定时器
  if (aiGreetingTimer) clearInterval(aiGreetingTimer);

  // 构造一条 AI 消息对象
  const aiMsg = ref({
    messageId: Date.now(),
    sessionId: currentChatSession.value.sessionId,
    sendUserId: 'Urobot',
    messageContent: "",
    messageType: 2
  });

  messageList.value.push(aiMsg.value);

  let i = 0;
  // 使用定时器模拟流式输出打字机效果
  aiGreetingTimer = setInterval(() => {
    if (i < greetingText.length) {
      aiMsg.value.messageContent += greetingText.charAt(i);
      i++;
      // 根据需要决定是否每次字数增加都滚动到底部
      if (i % 5 === 0) goToBottom();
    } else {
      clearInterval(aiGreetingTimer);
    }
  }, 20);
}

const setSessionSelect = ({ contactId, sessionId }) => {
  window.ipcRenderer.send("setSessionSelect", { contactId, sessionId });
};

const loadChatMessage = () => {
  if (messageCountInfo.noData) {
    proxy.Message.warning("没有更多消息了");
    return;
  }
  messageCountInfo.pageNum++;
  window.ipcRenderer.send("loadChatMessage", {
    sessionId: currentChatSession.value.sessionId,
    pageNum: messageCountInfo.pageNum,
    maxMessageId: messageCountInfo.maxMessageId
  });
};

const onLoadSessionData = () => {
  window.ipcRenderer.on("loadSessionDataCallBack", (event, data) => {
    console.log("loadSessionDataCallBack:", data);
    sortChatSessionList(data);
    chatSessionList.value = data;

    let noReadCount = 0;
    data.forEach(item => {
      noReadCount += item.noReadCount;
    });
    messageCountStore.setCount("chatCount", noReadCount, true);

    if (currentChatSession.value.contactId) {
      const latestSession = data.find(item => item?.contactId === currentChatSession.value.contactId);
      if (latestSession) {
        currentChatSession.value = Object.assign({}, latestSession);
      }
    }
  });
};

const onReceiveMessage = () => {
  window.ipcRenderer.on("receiveMessage", (event, msg) => {
    console.log("收到消息:", msg);
    if (msg.messageType === 4) {
      loadContactApply();
      return;
    }
    if (msg.messageType === 6) {
      const localMessage = messageList.value.find(item => item.messageId === msg.messageId);
      if (localMessage !== null) {
        localMessage.status = msg.status;
      }
      return;
    }

    if (msg.messageType === 7) { //强制下线
      proxy.Confirm({
        message: "您已被管理员强制下线，请重新登录",
        okfun: () => {
          setTimeout(() => {
            window.ipcRenderer.send("reLogin");
          }, 1000);
        },
        showCancelButton: false
      });
      return;

    }
    if (msg.messageType === 10) {
      let session = chatSessionList.value.find(item => item.sessionId === msg.sessionId);
      session.contactName = msg.extendData;
      return;
    }

    if (msg.messageType === 14) {
      proxy.Message.error("对方消息停止发送");
      messageList.value = messageList.value.filter(item => item.messageId !== msg.messageId);
      return;
    }

    const currentSession = chatSessionList.value.find(item => item.sessionId === msg.sessionId);
    if (currentSession === null || currentSession === undefined) {
      chatSessionList.value.push(msg.extendData);
    } else {
      Object.assign(currentSession, msg.extendData);
    }
    sortChatSessionList(chatSessionList.value);
    if (currentChatSession !== null && currentChatSession.value.sessionId !== msg.sessionId) {
      messageCountStore.setCount("chatCount", 1, false);
      return;
    }
    Object.assign(currentChatSession.value, msg.extendData);
    messageList.value.push(msg);
    goToBottom();
  });
};

const onLoadChatMessageCallBack = () => {
  window.ipcRenderer.on("loadChatMessageCallBack", (event, { dataList, pageTotal, pageNum }) => {
    if (pageNum === pageTotal) {
      messageCountInfo.noData = true;
    }
    dataList.sort((a, b) => a.messageId - b.messageId);
    const lastMessage = messageList.value[0];
    messageList.value = dataList.concat(messageList.value);
    messageCountInfo.pageNum = pageNum;
    messageCountInfo.pageTotal = pageTotal;
    if (pageNum === 1) {
      messageCountInfo.maxMessageId = dataList.length > 0 ? dataList[dataList.length - 1].messageId : null;
      goToBottom();
    } else {
      nextTick(() => {
        document.querySelector("#message" + lastMessage.messageId).scrollIntoView();
      })
    }
    console.log("loadChatMessageCallBack:", messageList.value);
  });
};

const onAddLocalMessage = () => {
  window.ipcRenderer.on("addLocalMessageCallBack", (event, { success, status, messageId, errorInfo }) => {
    const findMessage = messageList.value.find(item => item.messageId === messageId);
    if (findMessage && findMessage.status !== null) {
      findMessage.status = status;
    }
    console.log("addLocalMessageCallBack:", { success, status, messageId, errorInfo });
    if (success === false) {
      messageList.value = messageList.value.filter(item => item.messageId !== messageId);
      loadChatSession();
      proxy.Message.error(errorInfo || { message: "消息发送失败" });
      deleteMessage(messageId);
    }
  });
};

const deleteMessage = async (messageId) => {
  let result = await proxy.Request({
    url: proxy.Api.deleteMessage,
    showLoading: false,
    method: "delete",
    params: {
      messageId
    }
  })
  if (!result) {
    return;
  }
};
const sendMessage4Local = (messageObj) => {
  messageList.value.push(messageObj);
  const chatSession = chatSessionList.value.find(item => item.sessionId === messageObj.sessionId);
  if (chatSession) {
    chatSession.lastReceiveTime = messageObj.sendTime;
    // AI会话的lastMessage由generateAndSetAiSessionTitle设置（标题），不随后续消息更新
    if (currentChatSession.value.contactId !== AI_CONTACT_ID.value) {
      chatSession.lastMessage = messageObj.lastMessage;
    }
  }
  sortChatSessionList(chatSessionList.value);
  goToBottom();
  
};

const generateAndSetAiSessionTitle = async (sessionId) => {
  let result = await proxy.Request({
    url: proxy.Api.generateAiSessionTitle,
    showLoading: false,
    method: "get",
    params: {
      memoryId: userInfoStore.getInfo().userId
    }
  })
  if (!result || !result.data) {
    return;
  }
  // console.log("newTitle:", result.data);
  const chatSession = chatSessionList.value.find(item => item.sessionId === sessionId);
  if (chatSession) {
    chatSession.lastReceiveTime = Date.now();
    chatSession.lastMessage = result.data;
  }
  sortChatSessionList(chatSessionList.value);
  window.ipcRenderer.send("addLocalMessage", {
    lastMessage: result.data,
    lastReceiveTime: Date.now(),
    contactId: currentChatSession.value.contactId
  });
  await updateServerSessionInfo(sessionId, {
    lastMessage: result.data,
    lastReceiveTime: Date.now()
  });
}

const updateServerSessionInfo = async (sessionId, { lastMessage, lastReceiveTime }) => {
  let result = await proxy.Request({
        url: proxy.Api.sendMessage,
        showLoading: false,
        method: 'post',
        params: {
            messageContent: lastMessage,
            contactId: AI_CONTACT_ID.value,
            messageType: 2,
        },
        showError: false,
    })
    if (!result) {
        return;
    }
}

const handleCreateAiMessage = (aiMessageObj) => {
  console.log("用户消息对象：", messageList.value);
  isAIReplying.value = true;
  messageList.value.push(aiMessageObj);
  goToBottom();
}

const handleAiReplyEnd = () => {
  isAIReplying.value = false;
  if (!hasGeneratedTitle.value && currentChatSession.value.contactId === AI_CONTACT_ID.value) {
    hasGeneratedTitle.value = true;
    generateAndSetAiSessionTitle(currentChatSession.value.sessionId);
  }
}

const handleMessageFinished = (completeAiMsgObj) => {
  console.log("结束消息：", completeAiMsgObj.messageContent);
  console.log("用户消息对象：", messageList.value);
  // window.ipcRenderer.send("addLocalMessage", completeAiMsgObj);
}


const goToBottom = () => {
  nextTick(() => {
    if (distanceBottom > 200) {
      return;
    }
    const items = document.querySelectorAll(".message-item");
    if (items.length > 0) {
      setTimeout(() => {
        items[items.length - 1].scrollIntoView({ behavior: "smooth", block: 'end' });
      }, 1000);
    }
  });
};

const chatGroupDetailRef = ref(null);
const showChatGroupDetail = () => {
  chatGroupDetailRef.value.show(currentChatSession.value.contactId);
};

const loadContactApply = () => {
  window.ipcRenderer.send("loadContactApply");
};

const onLoadContactApply = () => {
  window.ipcRenderer.on("loadContactApplyCallBack", (event, contactNoReadCount) => {
    console.log("contactNoRead:", contactNoReadCount);
    messageCountStore.setCount("contactApplyCount", contactNoReadCount, true);
  });
};


const jumpToMessage = (contactId) => {
  let session = chatSessionList.value.find(item => item.contactId === contactId);
  if (!session) {
    window.ipcRenderer.send("reloadChatSession", { contactId });
    return;
  } else {
    chatSessionClickHandler(session);
  }
};

watch(
  () => route.query.contactId,
  (newValue, oldValue) => {
    if (newValue && route.query.contactId) {
      jumpToMessage(route.query.contactId);
    }
  },
  { immediate: true, deep: true }
)

const searchValue = ref();
const searchList = ref([]);
const search = () => {
  if (!searchValue.value) {
    return;
  }
  searchList.value = [];
  const regex = new RegExp("(" + searchValue.value + ")", "gi");
  chatSessionList.value.forEach(item => {
    if (item.contactName.includes(searchValue.value) || item.lastMessage.includes(searchValue.value)) {
      let newData = Object.assign({}, item);
      newData.searchContactName = item.contactName.replace(regex, "<span class='highlight'>$1</span>");
      newData.searchLastMessage = item.lastMessage.replace(regex, "<span class='highlight'>$1</span>");
      searchList.value.push(newData);
    }
  });
};

const searchClickHandler = (item) => {
  // searchValue.value = undefined;
  // searchList.value = [];
  chatSessionClickHandler(item);
};

const onReloadChatSessionCallBack = () => {
  window.ipcRenderer.on("reloadChatSessionCallBack", (event, { contactId, chatSessionList }) => {
    // console.log("reloadChatSessionCallBack:", { contactId, sessionId });
    chatSessionList.value = chatSessionList;
    sortChatSessionList(chatSessionList.value);
    jumpToMessage(contactId);
  });
};
onMounted(() => {
  onReceiveMessage();
  loadChatSession();
  onLoadSessionData();
  onLoadChatMessageCallBack();
  onAddLocalMessage();
  loadContactApply();
  onLoadContactApply();
  onReloadChatSessionCallBack();
  nextTick(() => {
    const messagePanel = document.querySelector("#message-panel");
    messagePanel.addEventListener("scroll", (e) => {
      const scrollTop = messagePanel.scrollTop;
      distanceBottom = e.target.scrollHeight - e.target.clientHeight - scrollTop;
      if (scrollTop === 0 && messageList.value.length > 0 && currentChatSession.value.contactId !== AI_CONTACT_ID.value) {
        loadChatMessage();
      }
    });
  });
  setSessionSelect({});
});

onUnmounted(() => {
  window.ipcRenderer.removeAllListeners("loadSessionDataCallBack");
  window.ipcRenderer.removeAllListeners("receiveMessage");
  window.ipcRenderer.removeAllListeners("loadChatMessageCallBack");
  window.ipcRenderer.removeAllListeners("addLocalMessageCallBack");
  window.ipcRenderer.removeAllListeners("loadContactApplyCallBack");
  window.ipcRenderer.removeAllListeners("reloadChatSessionCallBack");
});

onDeactivated(() => {
  setSessionSelect({});
  currentChatSession.value = {};
})
</script>

<style lang="scss" scoped>
.drag-panel {
  height: 25px;
  background: #f7f7f7;
}

.top-search {
  padding: 0px 10px 9px 10px;
  background: #f7f7f7;
  display: flex;
  align-items: center;

  .iconfont {
    font-size: 12px;
  }
}

.chat-session-list {
  height: calc(100vh - 62px);
  overflow: hidden;
  border-top: 1px solid #ddd;

  &:hover {
    overflow: auto;
  }
}

.search-list {
  height: calc(100vh - 62px);
  background: #f7f7f7;
  overflow: hidden;

  &:hover {
    overflow: auto;
  }
}

.title-panel {
  display: flex;
  align-items: center;

  .title {
    height: 58.2px;
    line-height: 60px;
    padding-left: 10px;
    font-size: 18px;
    color: #000000;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.icon-more {
  position: absolute;
  z-index: 1;
  top: 30px;
  right: 3px;
  width: 20px;
  font-size: 20px;
  margin-right: 5px;
  cursor: pointer;
}

.chat-panel {
  border-top: 1px solid #ddd;
  background: #f5f5f5;

  .message-panel {
    padding: 10px 30px 0 30px;
    height: calc(100vh - 260px);
    overflow-y: auto;

    .message-item {
      margin-bottom: 15px;
      text-align: center;
    }
  }
}
</style>
