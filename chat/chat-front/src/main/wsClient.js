import WebSocket from "ws";
const NODE_ENV = process.env.NODE_ENV;

import store from "./store";
import { saveOrUpdateChatSessionBatch4Init, saveOrUpdateMessage4Session, selectUserSessionByContactId, updateGroupName } from "./db/ChatSessionUserModel";
import { saveMessage, saveMessageBatch, updateMessage, deleteMessage } from "./db/ChatMessageModel";
import { updateContactNoReadCount } from "./db/UserSettingModel";

let ws = null;
let maxReConnectTimes = null;
let lockReconnect = false;

let wsUrl = null;
let sender = null;
let needReconnect = null;
const initWs = (config, _sender) => {
    wsUrl = `${NODE_ENV !== "development" ? store.getData("prodWsDomain") : store.getData("devWsDomain")} ? token=${config.token}`;
    sender = _sender;
    needReconnect = true;
    maxReConnectTimes = 5;
    createWs();
}

const createWs = () => {
    if (wsUrl === null) {
        return
    }
    ws = new WebSocket(wsUrl);
    ws.onopen = function () {
        console.log("客户端连接成功")
        ws.send("heart beat")
        maxReConnectTimes = 5
    }

    // 从服务器接受到信息的回调函数
    ws.onmessage = async function (event) {
        console.log("服务器返回信息", event.data)
        const message = JSON.parse(event.data)
        const messageType = message.messageType
        switch (messageType) {
            case 0:
                // 保存会话信息
                console.log("保存会话信息")
                await saveOrUpdateChatSessionBatch4Init(message.extendData.chatSessionList)
                // 保存消息
                console.log("保存具体消息")
                await saveMessageBatch(message.extendData.chatMessageList)
                //更新联系人数量
                await updateContactNoReadCount({ userId: store.getUserId(), noReadCount: message.extendData.applyCount })
                //发送消息
                sender.send("receiveMessage", { messageType: messageType })
                break;
            case 1://添加好友成功
            case 2://聊天消息
            case 3:// 群聊创建成功
            case 5://图、视频、文件消息
            case 8://解散群聊
            case 9://好友加入群组
            case 11://退出群聊
            case 12://踢出群聊
                if (message.sendUserId === store.getUserId() && message.contactType === 1) {
                    break;
                }
                console.log("message:", message)
                if (message.messageType === 11 && message.extendData === store.getUserId()) {
                    break;
                }

                const sessionInfo = {};
                if (message.extendData && typeof message.extendData === "object") {
                    Object.assign(sessionInfo, message.extendData)
                } else {
                    Object.assign(sessionInfo, message)
                    if (message.contactType === 0 && messageType != 1) {
                        sessionInfo.contactName = message.sendUserNickName
                    }
                    sessionInfo.lastReceiveTime = message.sendTime
                }
                //加入、退出群聊
                if ([9, 11, 12].includes(message.messageType)) {
                    sessionInfo.memberCount = message.memberCount;
                }

                await saveOrUpdateMessage4Session(store.getUserData("currentSessionId"), sessionInfo)
                //写入本地消息
                await saveMessage(message)
                const dbSessionInfo = await selectUserSessionByContactId(message.contactId)
                message.extendData = dbSessionInfo
                sender.send("receiveMessage", message)
                break
            case 4://好友申请消息
                await updateContactNoReadCount({ userId: store.getUserId(), noReadCount: 1 })
                sender.send("receiveMessage", { messageType: messageType })
                break;
            case 6://文件上传完成
                // console.log("文件上传完成:", message)
                updateMessage({ status: message.status }, { messageId: message.messageId });
                sender.send("receiveMessage", message);
                break;
            case 7://强制下线
                sender.send("receiveMessage", message);
                closeWs();
                break;

            case 10://修改群昵称
                updateGroupName(message.contactId, message.extendData);
                sender.send("receiveMessage", message);
                break;
            case 14:
                // console.log("删除消息:", message)
                deleteMessage(message.messageId);
                sender.send("receiveMessage", message);
                break;
            default:
                break;
        }
    }

    ws.onclose = function () {
        console.log("连接已关闭")
        reconnect()
    }

    ws.onerror = function () {
        console.log("连接失败，准备重连")
        reconnect()
    }

    const reconnect = () => {
        if (!needReconnect) {
            console.log("连接断开，不需要重连")
            return
        }
        if (ws !== null) {
            ws.close()
        }
        if (lockReconnect) {
            return
        }
        lockReconnect = true
        if (maxReConnectTimes > 0) {
            console.log("准备第" + (6 - maxReConnectTimes) + "次重连", new Date().getTime())
            maxReConnectTimes--
            setTimeout(() => {
                createWs()
                lockReconnect = false
            }, 5000)

        } else {
            console.log("连接超时")

        }
    }

    setInterval(() => {
        if (ws !== null && ws.readyState === 1) {
            // console.log("发送心跳包")
            ws.send("heart beat")
        }
    }, 5000);
}

const closeWs = () => {
    needReconnect = false;
    ws.close()
}

export {
    initWs,
    createWs,
    closeWs
}
