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
import { updateNoReadCount } from './ChatSessionUserModel.js';

const saveMessage = (data) => {
    data.userId = store.getUserId();
    return insertOrReplace('chat_message', data);
}
const saveMessageBatch = (messageList) => {
    return new Promise(async (resolve, reject) => {
        const chatSessionCountMap = {};
        messageList.forEach(message => {
            let contactId = message.contactType === 1 ? message.contactId : message.sendUserId;
            let noReadCount = chatSessionCountMap[contactId];
            if (!noReadCount) {
                chatSessionCountMap[contactId] = 1;
            } else {
                chatSessionCountMap[contactId] = noReadCount + 1;
            }
        });

        // 更新未读数
        for (let contactId in chatSessionCountMap) {
            await updateNoReadCount({ contactId: contactId, noReadCount: chatSessionCountMap[contactId] });
        }

        // 批量插入
        messageList.forEach(async message => {
            await saveMessage(message);
        });
        resolve();
    })
}

const getPageOffset = (pageNum, totalCount) => {
    const pageSize = 20;
    if (totalCount === 0) {
        return { pageTotal: 0, offset: 0, limit: pageSize };
    }
    const pageTotal = Math.ceil(totalCount / pageSize);
    pageNum = pageNum <= 1 ? 1 : pageNum;
    pageNum = pageNum >= pageTotal ? pageTotal : pageNum;
    return { pageTotal, offset: (pageNum - 1) * pageSize, limit: pageSize };
}

const selectMessageById = (messageId) => {
    let sql = `select * from chat_message where message_id = ? and user_id = ?`;
    const params = [messageId, store.getUserId()];
    return queryOne(sql, params);
}

const selectMessageList = ({ sessionId, pageNum, maxMessageId }) => {
    return new Promise(async (resolve, reject) => {
        let sql = `select count(1) from chat_message where session_id = ? and user_id = ?`;
        const totalCount = await queryCount(sql, [sessionId, store.getUserId()]);
        const { pageTotal, offset, limit } = getPageOffset(pageNum, totalCount);
        console.log('pageTotal', pageTotal);
        console.log('pageNum', pageNum);
        if (pageNum > pageTotal) {
            return resolve({
                dataList: [],
                pageTotal,
                pageNum
            });
        }

        const params = [sessionId, store.getUserId()];
        sql = `select * from chat_message where session_id = ? and user_id = ?`;
        if (maxMessageId) {
            sql += ` and message_id <= ?`;
            params.push(maxMessageId);
        }
        sql += ` order by message_id desc limit ? offset ?`;
        params.push(limit);
        params.push(offset);
        const dataList = await queryAll(sql, params);
        return resolve({
            dataList,
            pageTotal,
            pageNum
        });
    })
}

const updateMessage = (data, paramData) => {
    paramData.userId = store.getUserId();
    return update('chat_message', data, paramData);
}

const deleteMessage = (messageId) => {
    let sql = `delete from chat_message where message_id = ? and user_id = ?`;
    const params = [messageId, store.getUserId()];
    return run(sql, params);
}

export {
    saveMessageBatch,
    selectMessageById,
    selectMessageList,
    saveMessage,
    updateMessage,
    deleteMessage
}