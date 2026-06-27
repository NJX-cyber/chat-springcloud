const File_Type = {
    jpg: 0, jpeg: 0, png: 0, gif: 0, bmp: 0, webp: 0, svg: 0,
    mp4: 1, avi: 1, mov: 1, mkv: 1, webm: 1,
    0: "图片", 1: "视频",2: "文件"
}

const getFileType = (suffix) => {
    if (suffix === null || suffix === undefined) {
        return 2;
    }
    if (typeof suffix === "string") {
        suffix = suffix.toLowerCase();
    }
    const fileType = File_Type[suffix];
    if (fileType === undefined || fileType === null) {
        return 2;
    }
    return fileType;
}

export {
    getFileType
}