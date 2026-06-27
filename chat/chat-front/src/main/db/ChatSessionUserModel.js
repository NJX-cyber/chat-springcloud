import {
    run,
    queryAll,
    queryOne,
    queryCount,
    insertOrReplace,
    insertOrIgnore,
    update,
    insert
} from './ADB.js';
import store from '../store.js';

const selectUserSessionByContactId = (contactId) => {
    let sql = `select * from chat_session_user where user_id = ? and contact_id = ?`;
    return queryOne(sql, [store.getUserId(), contactId]);
}

const addChatSession = (chatSession) => {
    chatSession.userId = store.getUserId();
    insertOrIgnore("chat_session_user", chatSession)
}

const updateChatSession = (chatSession) => {
    const params = {
        userId: store.getUserId(),
        contactId: chatSession.contactId
    };
    const updateInfo = Object.assign({}, chatSession);
    updateInfo.userId = null;
    updateInfo.contactId = null;

    return update("chat_session_user", updateInfo, params);
}

const saveOrUpdateChatSessionBatch4Init = async (chatSessionList) => {
    try {
        for (let i = 0; i < chatSessionList.length; i++) {
            const chatSession = chatSessionList[i];
            chatSession.status = 1;

            let sessionData = await selectUserSessionByContactId(chatSession.contactId);

            if (sessionData) {
                await updateChatSession(chatSession);
            } else {
                await addChatSession(chatSession);
            }
        }
    } catch (e) {
        console.log("saveOrUpdateChatSessionBatch4Init error: ", e);
    }
};


// 更新未读数
const updateNoReadCount = ({ contactId, noReadCount }) => {
    let sql = `update chat_session_user set no_read_count = no_read_count+? where user_id = ? and contact_id = ?`;
    run(sql, [noReadCount, store.getUserId(), contactId]);
}

const selectUserSessionList = () => {
    let sql = `select * from chat_session_user where user_id = ? and status = 1`;
    return queryAll(sql, [store.getUserId()]);
}

const delChatSession = (contactId) => {
    const paramData = {
        userId: store.getUserId(),
        contactId: contactId
    }
    const sessionInfo = {
        status: 0
    }
    return update("chat_session_user", sessionInfo, paramData);
}

const topChatSession = (contactId, topType) => {
    const paramData = {
        userId: store.getUserId(),
        contactId: contactId
    }
    const sessionInfo = {
        topType: topType
    }
    return update("chat_session_user", sessionInfo, paramData);
}

const updateSessionInfo4Message = async (currentSessionId, { sessionId, contactName, lastMessage, lastReceiveTime, contactId, memberCount }) => {
    let sql = `update chat_session_user set last_message = ?, last_receive_time = ?, status = 1`;
    const params = [lastMessage, lastReceiveTime];
    if (contactName) {
        sql += `, contact_name = ?`;
        params.push(contactName);
    }
    //成员数量
    if (memberCount) {
        sql += `, member_count = ?`;
        params.push(memberCount);
    }
    //未选中当前session增加未读消息数
    if (currentSessionId === null || sessionId != currentSessionId) {
        sql += `, no_read_count = no_read_count+1`;

    }
    sql += ` where user_id = ? and contact_id = ?`;
    params.push(store.getUserId(), contactId);
    return run(sql, params);
}

const readAll = (contactId) => { 
    let sql = `update chat_session_user set no_read_count = 0 where user_id = ? and contact_id = ?`;
    return run(sql, [store.getUserId(), contactId]);
}

const saveOrUpdateMessage4Session = (currentSessionId, sessionInfo) => { 
    return new Promise(async (resolve, reject) => { 
        let sessionData = await selectUserSessionByContactId(sessionInfo.contactId);
        if (sessionData) { 
            updateSessionInfo4Message(currentSessionId, sessionInfo);
        } else {
            sessionInfo.noReadCount = 1;
            await addChatSession(sessionInfo);
        }
        resolve();
    })
}

const updateGroupName = (contactId, groupName) => {
    const paramData = {
        userId: store.getUserId(),
        contactId: contactId
    };
    const sessionInfo = {
        contactName: groupName
    };
    return update("chat_session_user", sessionInfo, paramData);
}

const updateSessionStatus = (contactId, status) => { 
    const paramData = {
        userId: store.getUserId(),
        contactId: contactId
    };
    const sessionInfo = {
        status: status
    };
    return update("chat_session_user", sessionInfo, paramData);
}

export {
    saveOrUpdateChatSessionBatch4Init,
    updateNoReadCount,
    selectUserSessionList,
    delChatSession,
    topChatSession,
    updateSessionInfo4Message,
    readAll,
    saveOrUpdateMessage4Session,
    selectUserSessionByContactId,
    updateGroupName,
    updateSessionStatus
};