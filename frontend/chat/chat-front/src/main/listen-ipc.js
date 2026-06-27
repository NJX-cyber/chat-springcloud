import { ipcMain, BrowserWindow } from 'electron'
import { join } from 'path'
import icon from '../../resources/icon.png?asset'
import { is } from '@electron-toolkit/utils'
import store from './store'
import { initWs, closeWs } from './wsClient'
import { addUserSetting, selectSettingInfo, updateContactNoReadCount, selectLocalUser } from './db/UserSettingModel'
import { selectUserSessionList, delChatSession, topChatSession, updateSessionInfo4Message, readAll, updateSessionStatus } from './db/ChatSessionUserModel'
import { selectMessageList, saveMessage, deleteMessage } from './db/ChatMessageModel'
import { saveFile2Local, createCover, saveFile, saveClipBoardFile, stopExpressServer, openLocalFolder, getLocalFileFolder, changeLocalFolder, downLoadUpdate } from './file'
import { getWindow, saveWindow, delWindow } from './windowProxy'
const onLoginOrRegister = (callback) => {
    ipcMain.on("loginOrRegister", (event, isLogin) => {
        console.log("登录状态变更:", isLogin)
        callback(isLogin)
    })
}

const onLoginSuccess = (callback) => {
    ipcMain.on("loginSuccess", async (event, config) => {
        store.initUserId(config.userId)
        store.setUserData("token", config.token)
        await addUserSetting(config.userId, config.email)
        callback(config)
        initWs(config, event.sender)
        event.sender.send("loginSuccessCallBack")
    })
}

const winTitleOp = (callback) => {
    ipcMain.on("winTitleOp", (e, data) => {
        callback(e, data)
    })
}

const onSetLocalStore = () => {
    ipcMain.on("setLocalStore", (e, { key, value }) => {
        store.setData(key, value)
    })
}

const onGetLocalStore = () => {
    ipcMain.on("getLocalStore", (e, key) => {
        e.sender.send("getLocalStoreCallBack", store.getData(key))
    })
}

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

const onLoadSessionData = () => {
    ipcMain.on("loadSessionData", async (e) => {
        while (!store.getUserId()) {
            await sleep(500)
        }
        const dataList = await selectUserSessionList()
        e.sender.send("loadSessionDataCallBack", dataList)
    })
}

const onDelChatSession = () => {
    ipcMain.on("delChatSession", async (e, { contactId }) => {
        delChatSession(contactId)
    })
}

const onTopChatSession = () => {
    ipcMain.on("topChatSession", async (e, { contactId, topType }) => {
        topChatSession(contactId, topType)
    })
}

const onLoadChatMessage = () => {
    ipcMain.on("loadChatMessage", async (e, data) => {
        const result = await selectMessageList(data)
        e.sender.send("loadChatMessageCallBack", result)
    })
}

const onSetSessionSelect = () => {
    ipcMain.on("setSessionSelect", async (e, { contactId, sessionId }) => {
        if (sessionId) {
            store.setUserData("currentSessionId", sessionId)
            readAll(contactId)
        } else {
            store.deleteUserData("currentSessionId")
        }
    })
}

const onAddLocalMessage = () => {
    ipcMain.on("addLocalMessage", async (e, data) => {
        try {
            // console.log("addLocalMessage: ", data);
            await saveMessage(data)
            if (data.messageType === 5) {
                await saveFile2Local({
                    messageId: data.messageId,
                    filePath: data.filePath,
                    fileType: data.fileType,
                    fileName: data.fileName,
                    sendTime: data.sendTime
                })
                data.status = 1
            }
            data.lastReceiveTime = data.sendTime
            updateSessionInfo4Message(store.getUserData("currentSessionId"), data)
            e.sender.send("addLocalMessageCallBack", {
                success: true,
                status: 1,
                messageId: data.messageId
            })
        } catch (error) {
            console.log("addLocalMessage error: ", error);
            e.sender.send("addLocalMessageCallBack", {
                success: false,
                status: -1,
                messageId: data.messageId,
                errorInfo: error.info
            })
            deleteMessage(data.messageId)
        }
    })
}

const onCreateCover = () => {
    ipcMain.on("createCover", async (e, { filePath, fileName }) => {
        console.log("createCover: ", filePath, fileName);
        try {
            const stream = await createCover(filePath, fileName);
            e.sender.send("createCoverCallBack", {
                success: true,
                ...stream
            })
        } catch (error) {
            e.sender.send("createCoverCallBack", {
                success: false,
                errorInfo: error
            })
        }
    })
}

const onNewWindow = () => {
    ipcMain.on("newWindow", async (e, config) => {
        openWindow(config)
    })
}

const openWindow = ({ windowId, title = "", path, width = 800, height = 720, data }) => {
    const localServerPort = store.getUserData("localServerPort");
    data.localServerPort = localServerPort;
    let win = getWindow(windowId);
    if (!win) {
        win = new BrowserWindow({
            icon: icon,
            width: width,
            height: height,
            fullscreen: false,
            fullscreenable: false,
            maximizable: false,
            autoHideMenuBar: false,
            titleBarStyle: 'hidden',
            resizable: false,
            frame: true,
            transparent: true,
            hasShadow: false,
            webPreferences: {
                preload: join(__dirname, '../preload/index.js'),
                sandbox: false,
                contextIsolation: false
            }
        })
        saveWindow(windowId, win);
        win.setMinimumSize(600, 484);
        if (is.dev && process.env['ELECTRON_RENDERER_URL']) {
            win.loadURL(`${process.env['ELECTRON_RENDERER_URL']}/index.html#${path}`);
        } else {
            win.loadFile(join(__dirname, `../renderer/index.html`), { hash: `${path}` });
        }
        win.on('ready-to-show', () => {
            win.show()
            win.setTitle(title)
            // 打开控制台（以独立新窗口形式）
            if (process.env.NODE_ENV === 'development') {
                win.webContents.openDevTools({ mode: 'detach' });

            }
        })
        win.once('show', () => {
            setTimeout(() => {
                win.webContents.send('pageInitData', data)
            }, 1000)
        })
    } else {
        win.show()
        win.setSkipTaskbar(false)
        win.webContents.send('pageInitData', data)
    }
}

const onSaveFile = () => {
    ipcMain.on("saveFile", async (e, data) => {
        saveFile(data);
    })
}

const onSaveClipBoardFile = () => {
    ipcMain.on("saveClipBoardFile", async (e, data) => {
        const result = await saveClipBoardFile(data);
        console.log("saveClipBoardFile result: ", result);
        e.sender.send("saveClipBoardFileCallBack", result);
    })
}

const onLoadContactApply = () => {
    ipcMain.on("loadContactApply", async (e) => {
        const result = await selectSettingInfo(store.getUserId());
        let contactNoReadCount = 0;
        if (result != null) {
            contactNoReadCount = result.contactNoRead;
        }
        e.sender.send("loadContactApplyCallBack", contactNoReadCount);
    })
}

const onUpdateContactNoReadCount = () => {
    ipcMain.on("updateContactNoReadCount", async (e) => {
        updateContactNoReadCount({
            userId: store.getUserId()
        })
    })
}

const onReLogin = (callback) => {
    ipcMain.on("reLogin", async (e) => {
        callback();
        e.sender.send("reLoginCallBack");
        closeWs();
        stopExpressServer();
    })
}

const onOpenLocalFolder = () => {
    ipcMain.on("openLocalFolder", async (e) => {
        openLocalFolder();
    })
}

const onGetSystemSetting = () => {
    ipcMain.on("getSystemSetting", async (e) => {
        const filePath = await getLocalFileFolder();
        e.sender.send("getSystemSettingCallBack", filePath);
    })
}

const onChangeLocalFolder = () => {
    ipcMain.on("changeLocalFolder", async (e) => {
        changeLocalFolder();
    })
}

const onReloadChatSession = () => {
    ipcMain.on("reloadChatSession", async (e, { contactId }) => {
        await updateSessionStatus(contactId, 1);
        const chatSessionList = await selectUserSessionList();
        e.sender.send("reloadChatSessionCallBack", { contactId, chatSessionList });
    })
}

const onDownloadUpdate = () => {
    ipcMain.on("downloadUpdate", async (e, { id, fileName }) => {
        downloadUpdate(id, fileName);
    })
}

const onOpenUrl = () => {
    ipcMain.on("openUrl", async (e, { url }) => {
        shell.openExternal(url);
    })
}

const onLoadLocalUser = () => { 
    ipcMain.on("loadLocalUser", async (e) => {
        const result = await selectLocalUser();
        e.sender.send("loadLocalUserCallBack", result);
    })
}

export {
    onLoginOrRegister,
    onLoginSuccess,
    winTitleOp,
    onSetLocalStore,
    onGetLocalStore,
    onLoadSessionData,
    onDelChatSession,
    onTopChatSession,
    onLoadChatMessage,
    onAddLocalMessage,
    onSetSessionSelect,
    onCreateCover,
    onNewWindow,
    onSaveFile,
    onSaveClipBoardFile,
    onLoadContactApply,
    onUpdateContactNoReadCount,
    onReLogin,
    onOpenLocalFolder,
    onGetSystemSetting,
    onChangeLocalFolder,
    onReloadChatSession,
    onDownloadUpdate,
    onOpenUrl,
    onLoadLocalUser,
    openWindow,
}
