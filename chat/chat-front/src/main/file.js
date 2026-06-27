const fs = require('fs');
const fse = require('fs-extra');
const NODE_ENV = process.env.NODE_ENV;
const path = require('path');
const { app, ipcMain, shell, dialog } = require('electron');
const { exec } = require('child_process');
const FormData = require('form-data');
const axios = require('axios');
import store from "./store";
const moment = require('moment');
moment.locale('zh-cn', {});
import { uploadRequest, downloadStreamRequest } from "./request";
import { getWindow } from "./windowProxy";

//express服务器
const express = require('express');
const expressServer = express();

import { selectMessageById } from "./db/ChatMessageModel";
import { selectSettingInfo, updateSystemSetting } from "./db/UserSettingModel";
const cover_image_suffix = "_cover.png"
const image_suffix = ".png"

const ffprobePath = "/assets/ffprobe.exe"
const ffmpegPath = "/assets/ffmpeg.exe"

const getDomain = () => {
    return NODE_ENV !== "development" ? store.getData("prodDomain") : store.getData("devDomain");
};

const getResourcesPath = () => {
    let resourcesPath = app.getAppPath();
    if (NODE_ENV !== "development") {
        resourcesPath = path.dirname(app.getPath("exe") + "/resources");
    }
    return resourcesPath;
}

const getFFprobePath = () => {
    return path.join(getResourcesPath(), ffprobePath);
}

const getFFmpegPath = () => {
    return path.join(getResourcesPath(), ffmpegPath);
}

const removeFile = (filePath) => {
    if (fs.existsSync(filePath)) {
        fs.unlinkSync(filePath);
    }
}
const execCommand = (commend) => {
    return new Promise((resolve, reject) => {
        exec(commend, (error, stdout, stderr) => {
            console.log("ffmpeg commend: ", commend);
            if (error) {
                console.log("ffmpeg error: ", error);
                reject(error);
            } else {
                resolve(stdout);
            }
        });
    })
}

const mkDirs = (dir) => {
    // console.log("mkDirs dir: ", dir);
    if (!fs.existsSync(dir)) {
        const parentDir = path.dirname(dir);
        if (parentDir !== dir) {
            mkDirs(parentDir);
        }
        fs.mkdirSync(dir);
    }
}

const uploadFile = (messageId, savePath, coverPath) => {
    console.log("uploadFile messageId: ", messageId, " savePath: ", savePath, " coverPath: ", coverPath);
    const formData = new FormData();
    formData.append("messageId", messageId);
    formData.append("file", fs.createReadStream(savePath));
    if (coverPath) {
        formData.append("cover", fs.createReadStream(coverPath));
    }
    const url = `${getDomain()}/api/chat/uploadFile`;

    return uploadRequest(url, formData)
        .then(res => {
            // console.log("uploadFile success: ", res);
        }).catch(err => {
            // console.log("uploadFile err: ", err);
            removeFile(savePath);
            if (coverPath) {
                removeFile(coverPath);
            }
            throw err;
        });
}

const saveFile2Local = ({ messageId, filePath, fileType }) => {
    console.log("saveFile2Local messageId: ", messageId, " filePath: ", filePath, " fileType: ", fileType);
    return new Promise(async (resolve, reject) => {
        let ffmpegPath = getFFmpegPath();
        let savePath = await getLocalFilePath("chat", false, messageId);
        fs.copyFileSync(filePath, savePath);
        let coverPath = null;
        if (fileType !== 2) {
            let commend = `${getFFprobePath()} -v error -select_streams v:0 -show_entries stream=codec_name "${filePath}"`;
            let result = await execCommand(commend);
            result = result.replaceAll("/r/n", "");
            result = result.substring(result.indexOf("=") + 1);
            let codec = result.substring(0, result.indexOf("["));
            if (codec === "hevc") {
                commend = `${ffmpegPath} -y -i "${filePath}" -c:v libx264 -crf 20 "${savePath}"`;
                await execCommand(commend);
            }
            coverPath = savePath.substring(0, savePath.lastIndexOf(".")) + cover_image_suffix;
            commend = `${ffmpegPath} -i "${savePath}" -y -vframes 1 -vf "scale=min(170\\,iw*min(170/iw\\,170/ih)):min(170\\,ih*min(170/iw\\,170/ih))" "${coverPath}"`;
            await execCommand(commend);

        }
        try {
            await uploadFile(messageId, savePath, coverPath);
        } catch (err) {
            // console.log("saveFile2Local 捕获到上传错误:", err);
            return reject(err);
        }
        resolve();
    })
}

const getLocalFilePath = (partType, showCover, fileId) => {
    return new Promise(async (resolve, reject) => {
        console.log("getLocalFilePath partType: ", partType, " showCover: ", showCover, " fileId: ", fileId);
        let localFolder = store.getUserData("localFileFolder");
        let localPath = null;
        if (partType === "avatar") {
            localFolder = path.join(localFolder, "avatar");
            if (!fs.existsSync(localFolder)) {
                mkDirs(localFolder);
            }
            localPath = path.join(localFolder, fileId + image_suffix);
        } else if (partType === "chat") {
            let messageInfo = await selectMessageById(fileId);
            const month = moment(messageInfo.sendTime).format("YYYYMM");
            localFolder = path.join(localFolder, month);
            if (!fs.existsSync(localFolder)) {
                mkDirs(localFolder);
            }
            let fileName = messageInfo.fileName;
            let fileSuffix = path.extname(fileName);
            localPath = path.join(localFolder, fileId + fileSuffix);
        } else if (partType === "tmp") {
            localFolder = path.join(localFolder, "tmp");
            if (!fs.existsSync(localFolder)) {
                mkDirs(localFolder);
            }
            localPath = path.join(localFolder, fileId);
        }
        if (showCover) {
            localPath = localPath.substring(0, localPath.lastIndexOf(".")) + cover_image_suffix;
        }
        resolve(localPath);
    })
}

let server = null;
const startExpressServer = (serverPort) => {
    server = expressServer.listen(serverPort, () => {
        console.log(`express server listening on port ${serverPort}`);
    });
}

const stopExpressServer = () => {
    if (server) {
        server.close();
    }
}

const FILE_TYPE_CONTENT_TYPE = {
    "0": "image/",
    "1": "video/",
    "2": "application/octet-stream"
}

expressServer.get('/file', async (req, res) => {
    let { partType, fileType, fileId, showCover, forceGet } = req.query;
    console.log("get /file partType: ", partType, " fileType: ", fileType, " fileId: ", fileId, " showCover: ", showCover, " forceGet: ", forceGet);
    if (!partType || !fileId || fileId === undefined) {
        res.status(400).send("参数错误");
        return;
    }
    showCover = showCover === undefined ? false : Boolean(showCover);
    const localPath = await getLocalFilePath(partType, showCover, fileId);
    console.log("get /file localPath: ", localPath);
    try {
        if (!fs.existsSync(localPath) || forceGet === "true") {
            if (forceGet === "true" || partType === "avatar") {
                await downloadFile(fileId, true, String(localPath).endsWith(cover_image_suffix) ? localPath : localPath.substring(0, localPath.lastIndexOf(".")) + cover_image_suffix, partType, fileType);
            }
            await downloadFile(fileId, showCover, localPath, partType, fileType);
        }
    } catch (err) {
        console.log("downloadFile 捕获到错误:", err);
        return;
    }
    const fileSuffix = localPath.substring(localPath.lastIndexOf(".") + 1);
    let contentType = FILE_TYPE_CONTENT_TYPE[fileType] + fileSuffix;
    res.setHeader('Content-Type', contentType);
    res.setHeader('Access-Control-Allow-Origin', '*');
    if (showCover || fileType === "0") {
        fs.createReadStream(localPath).pipe(res);
        return;
    }
    let status = fs.statSync(localPath);
    let fileSize = status.size;
    let range = req.headers.range;
    if (range) {
        let parts = range.replace(/bytes=/, "").split("-");
        let start = parseInt(parts[0], 10);
        let end = parts[1] ? parseInt(parts[1], 10) : fileSize - 1;
        end = Math.min(end, fileSize - 1);
        let chunksize = (end - start) + 1;
        let fileStream = fs.createReadStream(localPath, { start, end });
        let headers = {
            'Content-Range': `bytes ${start}-${end}/${fileSize}`,
            'Accept-Ranges': 'bytes',
            'Content-Length': chunksize,
            'Content-Type': contentType,
        };
        res.writeHead(206, headers);
        fileStream.pipe(res);
    } else {
        let headers = {
            'Content-Length': fileSize,
            'Content-Type': contentType,
        };
        res.writeHead(200, headers);
        fs.createReadStream(localPath).pipe(res);
    }
    return;
});

const downloadFile = async (fileId, showCover, savePath, partType, fileType) => {
    console.log("downloadFile fileId: ", fileId, " showCover: ", showCover, " savePath: ", savePath, " partType: ", partType, " fileType: ", fileType);
    showCover = showCover + "";
    let url = `${getDomain()}/api/chat/downloadFile`;

    return new Promise(async (resolve, reject) => {
        const folder = path.dirname(savePath);
        mkDirs(folder);
        const stream = fs.createWriteStream(savePath);

        const { pipeline } = require('stream');
        const { promisify } = require('util');
        const pipelineAsync = promisify(pipeline);
        try {
            const fileStreamData = await downloadStreamRequest(url, { fileId, showCover });
            await pipelineAsync(fileStreamData, stream);
            resolve();
            return;
        } catch (err) {
            console.log("downloadFile 获取文件流异常: ", err);
            try {
                if (err?.code === 602) {
                    const resourcePath = getResourcesPath();
                    let filePath = "";
                    if (fileType === "0") {
                        filePath = partType === "avatar"
                            ? path.join(resourcePath, "assets/user.png")
                            : path.join(resourcePath, "assets/404.png");

                    } else if (fileType === "1") {
                        filePath = path.join(resourcePath, "assets/video-notFind.mp4")
                    }
                    // console.log("filePath: ", filePath);
                    // console.log("filePath: ", typeof fileType);
                    await pipelineAsync(
                        fs.createReadStream(filePath),
                        stream
                    );

                    resolve();
                    return;
                }
                stream.destroy();
                removeFile(savePath);
                return reject(err);
            } catch (fallbackErr) {
                reject(fallbackErr);
            }
        }
    })
}

const createCover = async (filePath, fileName) => {
    return new Promise(async (resolve, reject) => {
        let ffmpegPath = getFFmpegPath();
        let avatarPath = await getLocalFilePath("avatar", false, fileName + "_temp");
        let commend = `${ffmpegPath} -i "${filePath}" -y -vframes 1 "${avatarPath}" -y`;
        await execCommand(commend);

        let coverPath = avatarPath.substring(0, avatarPath.lastIndexOf("_")) + cover_image_suffix;
        commend = `${ffmpegPath} -i "${filePath}" -y -vframes 1 -vf "scale=170:170:force_original_aspect_ratio=increase,crop=170:170" "${coverPath}"`;
        await execCommand(commend);
        resolve({
            avatarStream: fs.readFileSync(avatarPath),
            coverStream: fs.readFileSync(coverPath)
        })
    })
}

const saveFile = async ({ partType, fileId }) => {
    let fileName = "";
    if (partType === "avatar") {
        fileName = fileId + image_suffix;
    } else if (partType === "chat") {
        let message = await selectMessageById(fileId);
        fileName = message.fileName;
    }
    const localPath = await getLocalFilePath(partType, false, fileId);
    const options = { title: "保存文件", defaultPath: fileName };
    let result = await dialog.showSaveDialog(options);
    if (result.canceled || !result.filePath) {
        return;
    }
    const savePath = result.filePath;
    fs.copyFileSync(localPath, savePath);
}

const saveClipBoardFile = async (file) => {
    const fileSuffix = file.name.substring(file.name.lastIndexOf("."));
    const localPath = await getLocalFilePath("tmp", false, "_tmp" + fileSuffix);
    let byteArray = file.byteArray;
    const buffer = Buffer.from(byteArray);
    fs.writeFileSync(localPath, buffer);
    return {
        size: buffer.length,
        name: file.name,
        path: localPath
    }
}

const getLocalFileFolder = async () => {
    let settingInfo = await selectSettingInfo(store.getUserId());
    return JSON.parse(settingInfo.sysSetting).localFileFolder;
}

const openLocalFolder = async () => {
    let localPath = await getLocalFileFolder();
    localPath = path.join(localPath, store.getUserId());
    if (!fs.existsSync(localPath)) {
        mkDir(localPath);
    }
    await shell.openPath("file:///" + localPath);
}

const isSamePath = (a, b) => {
    return path.resolve(a)
        .replace(/\\/g, "/")
        .toLowerCase()
        ===
        path.resolve(b)
            .replace(/\\/g, "/")
            .toLowerCase();
};
const changeLocalFolder = async () => {
    let localPath = await getLocalFileFolder();
    const options = {
        properties: ['openDirectory'],
        defaultPath: localPath
    };
    let result = await dialog.showOpenDialog(options);
    if (result.canceled || !result.filePaths) {
        return;
    }
    const newFolder = result.filePaths[0];
    const userId = store.getUserId();
    if (!isSamePath(localPath, newFolder)) {
        getWindow("main").webContents.send("copyFolderCallBack");
        await fse.copy(path.join(localPath, userId), path.join(newFolder, userId));
    }
    await updateSystemSetting({
        userId,
        fileFolder: newFolder + "\\"
    });
    const userFolder = newFolder + "\\" + userId;
    store.setUserData("localFileFolder", userFolder);
    getWindow("main").webContents.send("getSystemSettingCallBack", userFolder);
}

const downLoadUpdate = async (id, fileName) => {
    let url = `${store.getData("domain")}/api/update/download`;
    const token = store.getUserData("token");
    const config = {
        responseType: "stream",
        headers: {
            "Content-Type": "multipart/form-data",
            "token": token
        },
        onDownloadProgress: (progress) => {
            const loaded = progress.loaded;
            getWindow("main").webContents.send("updateDownloadCallBack", loaded);
        }
    }
    const response = await (url, { id }, config);
    const localFile = await getLocalFilePath(null, false, fileName);
    const stream = fs.createWriteStream(localFile);
    response.data.pipe(stream);
    stream.on('finish', () => {
        stream.close();
        // 开始安装
        const command = `${localFile}`;
        execCommand(command);
    });
}

export {
    saveFile2Local,
    startExpressServer,
    stopExpressServer,
    createCover,
    saveFile,
    saveClipBoardFile,
    getLocalFileFolder,
    openLocalFolder,
    changeLocalFolder,
    downLoadUpdate
}