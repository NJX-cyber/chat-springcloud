import moment from "moment";

const weekList = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
const isEmpty = (str) => {
    if (str === null || str === undefined || str.trim() === '') {
        return true;
    }
    return false;
}

const getAreaInfo = (area) => {
    if (isEmpty(area)) {
        return '-';
    }
    return area.replace(',', ' ');
}

const formatDate = (timeStamp) => {
    const timestamp = moment(timeStamp);
    const data = Number.parseInt(moment().format('YYYYMMDD')) - Number.parseInt(timestamp.format('YYYYMMDD'));
    if (data == 0) {
        return timestamp.format('HH:mm');
    } else if (data == 1) {
        return '昨天';
    } else if (data >= 2 && data < 7) {
        return weekList[timestamp.day()]
    } else {
        return timestamp.format('YYYY/MM/DD');
    }
}

const size2Str = (size) => {
    var sizeStr = "";
    if (size < 0.1 * 1024) { //小于0.1KB，则转化成B
        sizeStr = size.toFixed(2) + "B";
    } else if (size < 1024 * 1024) { //小于1MB，则转化成KB
        sizeStr = (size / 1024).toFixed(2) + "KB";
    } else if (size < 1024 * 1024 * 1024) { //小于1GB，则转化成MB
        sizeStr = (size / (1024 * 1024)).toFixed(2) + "MB";
    } else { //其他转化成GB
        sizeStr = (size / (1024 * 1024 * 1024)).toFixed(2) + "GB";
    }
    sizeStr = sizeStr + "";
    var index = sizeStr.indexOf(".");
    var dou = sizeStr.substring(index + 1, index + 3); //获取小数点后两位的值
    if (dou === "00") { //如果小数点后两位为00，则删除00
        return sizeStr.substring(0, index) + sizeStr.substring(index + 3, sizeStr.length);
    }
    return sizeStr;
}

export default {
    isEmpty,
    getAreaInfo,
    formatDate,
    size2Str
}