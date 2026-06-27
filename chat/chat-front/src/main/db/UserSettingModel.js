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
import { startExpressServer } from '../file.js';
const fs = require('fs');
const os = require('os');
const userDir = os.homedir();
const updateContactNoReadCount = ({ userId, noReadCount }) => {
    return new Promise(async (resolve, reject) => {
        let sql = null;
        if (noReadCount === 0) {
            resolve();
            return;
        }
        if (noReadCount) {
            sql = `update user_setting set contact_no_read = contact_no_read+? where user_id = ?`;
        } else {
            //清空未读数
            noReadCount = 0;
            sql = `update user_setting set contact_no_read = ? where user_id = ?`;
        }
        await run(sql, [noReadCount, userId]);
        resolve();
    })
}

const addUserSetting = async (userId, email) => {
    let sql = `select max(server_port) server_port from user_setting`
    let { serverPort } = await queryOne(sql, []);
    if (serverPort === null) {
        serverPort = 10340;
    } else {
        serverPort = serverPort + 1;
    }
    const sysSetting = {
        localFileFolder: userDir + '\\.chat\\fileStorage\\',
    }
    sql = `select * from user_setting where user_id = ?`
    const userInfo = await queryOne(sql, [userId])

    let resultServerPort = null;
    let localFileFolder = sysSetting.localFileFolder + userId;
    if (userInfo) {
        // await update("user_setting", { "email": email }, { "user_id": userId })
        resultServerPort = userInfo.serverPort;
        localFileFolder = JSON.parse(userInfo.sysSetting).localFileFolder + userId;
    } else {
        await insertOrIgnore("user_setting", {
            userId: userId,
            email: email,
            sysSetting: JSON.stringify(sysSetting),
            contactNoRead: 0,
            serverPort: serverPort
        })
        resultServerPort = serverPort;
        console.log('serverPort:',serverPort)
    }
    //TODO 启动本地服务
    fs.mkdirSync(localFileFolder, { recursive: true });
    startExpressServer(resultServerPort);
    store.setUserData("localServerPort", resultServerPort);
    // console.log('localFileFolder:',localFileFolder)
    store.setUserData("localFileFolder", localFileFolder);
}

const selectSettingInfo = (userId) => { 
    let sql = `select * from user_setting where user_id = ?`
    return queryOne(sql, [userId]);
}

const updateSystemSetting = ({userId, fileFolder}) => { 
    const data = { sysSetting: JSON.stringify({ localFileFolder: fileFolder}) }
    const params = { userId: userId }
    return update("user_setting", data, params)
}

const selectLocalUser = () => { 
    let sql = `select email from user_setting where email is not null`
    return queryAll(sql, []);
}

export {
    updateContactNoReadCount,
    addUserSetting,
    selectSettingInfo,
    updateSystemSetting,
    selectLocalUser
}
