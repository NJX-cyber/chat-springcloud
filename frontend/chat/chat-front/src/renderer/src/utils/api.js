const api = {
    prodDomain: "http://localhost:5050",
    devDomain: "http://localhost:8088",
    prodWsDomain: "ws://localhost:5050/ws",
    devWsDomain: "ws://localhost:5051/ws",
    checkCode: "/account/checkCode",// 获取验证码
    login: "/account/login",// 登录
    register: "/account/register",// 注册
    getSystemSettings: "/account/systemSettings",// 获取系统设置
    loadMyGroup: "/groupInfo/loadMyGroup",// 加载我创建的群聊
    saveGroup: "/groupInfo/saveGroup",// 创建/修改群聊
    getGroupInfo: "/groupInfo/getGroupInfo",// 获取群聊信息
    getGroupInfo4Chat: "/groupInfo/getGroupInfo4Chat",// 获取群聊信息（聊天界面）
    dissolutionGroup: "/groupInfo/dissolutionGroup",// 解散群聊
    leaveGroup: "/groupInfo/leaveGroup",// 退出群聊
    addOrRemoveGroupMember: "/groupInfo/addOrRemoveGroupMember",// 添加/移除群成员
    search: "/userContact/search",// 搜索用户/群聊
    applyAdd: "/userContact/applyAdd",// 申请添加好友/加入群聊
    loadApply: "/userContact/loadApply",// 获取好友申请列表
    dealWithApply: "/userContact/dealWithApply",// 处理好友申请
    loadContact: "/userContact/loadContact",// 加载联系人列表
    getContactInfo: "/userContact/getContactInfo",// 获取联系人信息
    getUserInfo: "/userInfo/getUserInfo",// 获取用户信息
    getUserContactInfo: "/userContact/getUserContactInfo",// 获取用户联系人信息
    addContact2BlackList: "/userContact/blacklistUserContact",// 加入黑名单
    delContact: "/userContact/delUserContact",// 删除好友
    saveUserInfo: "/userInfo/updateUserInfo",// 修改用户信息
    updatePassword: "/userInfo/updateUserPassword",// 修改密码
    logout: "/userInfo/logout",// 退出登录
    sendMessage: "/chat/sendMessage",// 发送消息
    deleteMessage: "/chat/deleteMessage",// 删除消息
    uploadFile: "/chat/uploadFile",// 上传文件
    checkVersion: "/systemVersion/checkVersion", // 检查版本
    loadAdminAccount: "/admin/loadUser", //加载用户列表
    loadBeautyAccountList: "/admin/loadBeautyAccountList", //加载用户列表
    saveBeautyAccount: "/admin/saveBeautyAccount", //保存靓号
    changeAccountStatus: "/admin/updateUserStatus", //修改用户状态
    forceOffLine: "/admin/forceOffLine", //强制下线
    delBeautyAccount: "/admin/delBeautyAccount", //删除靓号
    loadGroup: "/admin/loadGroup",// 加载群聊
    adminDissolutionGroup: "/admin/dissolutionGroup",// 解散群聊
    getSysSetting: "/admin/getSysSetting",// 获取系统设置
    saveSysSetting: "/admin/saveSysSetting",// 保存系统设置
    loadUpdateList: "/admin/loadUpdateList",// 加载更新列表
    saveUpdate: "/admin/saveUpdate",// 保存更新
    delUpdate: "/admin/delUpdate",// 删除更新
}

export default api
