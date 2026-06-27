import { app, shell, BrowserWindow, ipcMain, Tray, Menu } from 'electron'
import { join } from 'path'
import { electronApp, optimizer, is } from '@electron-toolkit/utils'
import icon from '../../resources/icon.png?asset'
const NODE_ENV = process.env.NODE_ENV

import {
  onLoginOrRegister, onLoginSuccess, winTitleOp, onSetLocalStore, onGetLocalStore, onLoadSessionData, onDelChatSession,
  onTopChatSession, onLoadChatMessage, onAddLocalMessage, onSetSessionSelect, onCreateCover, onNewWindow,
  onSaveFile, onSaveClipBoardFile, onLoadContactApply, onUpdateContactNoReadCount, onReLogin, onOpenLocalFolder, onGetSystemSetting, onChangeLocalFolder,
  onReloadChatSession, onDownloadUpdate, onOpenUrl, onLoadLocalUser, openWindow
} from './listen-ipc'
import { saveWindow } from './windowProxy'


const login_width = 300
const login_height = 370
const register_height = 490

function createWindow() {
  // Create the browser window.
  const mainWindow = new BrowserWindow({
    icon: icon,
    width: login_width,
    height: login_height,
    show: false,
    autoHideMenuBar: true,
    titleBarStyle: 'hidden',
    resizable: false,
    frame: true,
    transparent: false,
    ...(process.platform === 'linux' ? { icon } : {}),
    webPreferences: {
      preload: join(__dirname, '../preload/index.js'),
      sandbox: false,
      contextIsolation: false
    }
  })
  saveWindow("main", mainWindow);

  mainWindow.on('ready-to-show', () => {
    mainWindow.show()
    mainWindow.setTitle("Chat")
    // 打开控制台（以独立新窗口形式）
    if (NODE_ENV === 'development') {
      mainWindow.webContents.openDevTools({ mode: 'detach' });

    }
  })

  mainWindow.webContents.setWindowOpenHandler((details) => {
    shell.openExternal(details.url)
    return { action: 'deny' }
  })

  // HMR for renderer base on electron-vite cli.
  // Load the remote URL for development or the local html file for production.
  if (is.dev && process.env['ELECTRON_RENDERER_URL']) {
    mainWindow.loadURL(process.env['ELECTRON_RENDERER_URL'])
  } else {
    mainWindow.loadFile(join(__dirname, '../renderer/index.html'))
  }

  const tray = new Tray(icon)
  const contextMenu = [
    {
      label: '退出chat', click: function () {
        app.exit()
      }
    }
  ]

  const menu = Menu.buildFromTemplate(contextMenu)
  tray.setToolTip('chat')
  tray.setContextMenu(menu)
  tray.on('click', () => {
    mainWindow.setSkipTaskbar(false)
    mainWindow.show()
  })

  onLoginOrRegister((isLogin) => {
    mainWindow.setResizable(true);
    if (isLogin) {
      mainWindow.setSize(login_width, login_height)
    } else {
      mainWindow.setSize(login_width, register_height)
    }
    mainWindow.setResizable(false)
  })

  onLoginSuccess((config) => {
    mainWindow.setResizable(true);
    mainWindow.setSize(850, 800)
    mainWindow.center()
    mainWindow.setMaximizable(true)
    mainWindow.setMinimumSize(600, 600)

    if (config.admin) {
      contextMenu.unshift({
        label: "管理后台", click: function () {
          openWindow({
            windowId: "admin",
            title: "管理后台",
            path: "admin",
            width: config.screenWidth * 0.8,
            height: config.screenHeight * 0.8,
            data: {
              token: config.token
            }
          })
        }

      })
    }
    contextMenu.unshift({
      label: "用户：" + config.nickname, click: function () {
      }
    })
    tray.setContextMenu(Menu.buildFromTemplate(contextMenu))
  })

  winTitleOp((e, { action, data }) => {
    const webContents = e.sender;
    const win = BrowserWindow.fromWebContents(webContents);
    switch (action) {
      case "close":
        if (data.closeType === 0) {
          win.close()
        } else {
          win.setSkipTaskbar(true)
          win.hide()
        }
        break
      case "minimize":
        win.minimize()
        break
      case "maximize":
        win.maximize()
        break
      case "unmaximize":
        win.unmaximize()
        break
      case "top":
        win.setAlwaysOnTop(data.top)
        break
      default:
        break
    }
  })

  onReLogin((callback) => {
    mainWindow.setResizable(true);
    mainWindow.setMinimumSize(login_width, login_height);
    mainWindow.setSize(login_width, login_height);
    mainWindow.center();
    mainWindow.setResizable(false);
  })

  onSetLocalStore();
  onGetLocalStore();
  onLoadSessionData();
  onDelChatSession();
  onTopChatSession();
  onLoadChatMessage();
  onAddLocalMessage();
  onSetSessionSelect();
  onCreateCover();
  onNewWindow();
  onSaveFile();
  onSaveClipBoardFile();
  onLoadContactApply();
  onUpdateContactNoReadCount();
  onOpenLocalFolder();
  onGetSystemSetting();
  onChangeLocalFolder();
  onReloadChatSession();
  onDownloadUpdate();
  onOpenUrl();
  onLoadLocalUser();
}

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.whenReady().then(() => {
  // Set app user model id for windows
  electronApp.setAppUserModelId('com.electron')

  // Default open or close DevTools by F12 in development
  // and ignore CommandOrControl + R in production.
  // see https://github.com/alex8088/electron-toolkit/tree/master/packages/utils
  app.on('browser-window-created', (_, window) => {
    optimizer.watchWindowShortcuts(window)
  })

  createWindow()

  app.on('activate', function () {
    // On macOS it's common to re-create a window in the app when the
    // dock icon is clicked and there are no other windows open.
    if (BrowserWindow.getAllWindows().length === 0) createWindow()
  })
})

// Quit when all windows are closed, except on macOS. There, it's common
// for applications and their menu bar to stay active until the user quits
// explicitly with Cmd + Q.
app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit()
  }
})

// In this file you can include the rest of your app"s specific main process
// code. You can also put them in separate files and require them here.
