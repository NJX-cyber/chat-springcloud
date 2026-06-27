const api = {
    // ========== 统一网关入口 (Gateway port: 8800) ==========
    prodDomain: "http://localhost:8800",
    devDomain: "http://localhost:8800",

    // ========== WebSocket直连 (不经过网关) ==========
    prodWsDomain: "ws://localhost:5050/ws",
    devWsDomain: "ws://localhost:5051/ws",

    // ========== 账号相关 (user-info-manager) ==========
    checkCode: "/api/account/checkCode",// 获取验证码
    login: "/api/account/login",// 登录
    register: "/api/account/register",// 注册
    getSystemSettings: "/api/account/systemSettings",// 获取系统设置

    // ========== 群聊相关 (user-info-manager) ==========
    loadMyGroup: "/api/groupInfo/loadMyGroup",// 加载我创建的群聊
    saveGroup: "/api/groupInfo/saveGroup",// 创建/修改群聊
    getGroupInfo: "/api/groupInfo/getGroupInfo",// 获取群聊信息
    getGroupInfo4Chat: "/api/groupInfo/getGroupInfo4Chat",// 获取群聊信息（聊天界面）
    dissolutionGroup: "/api/groupInfo/dissolutionGroup",// 解散群聊
    leaveGroup: "/api/groupInfo/leaveGroup",// 退出群聊
    addOrRemoveGroupMember: "/api/groupInfo/addOrRemoveGroupMember",// 添加/移除群成员

    // ========== 联系人相关 (user-info-manager) ==========
    search: "/api/userContact/search",// 搜索用户/群聊
    applyAdd: "/api/userContact/applyAdd",// 申请添加好友/加入群聊
    loadApply: "/api/userContact/loadApply",// 获取好友申请列表
    dealWithApply: "/api/userContact/dealWithApply",// 处理好友申请
    loadContact: "/api/userContact/loadContact",// 加载联系人列表
    getContactInfo: "/api/userContact/getContactInfo",// 获取联系人信息
    getUserInfo: "/api/userInfo/getUserInfo",// 获取用户信息
    getUserContactInfo: "/api/userContact/getUserContactInfo",// 获取用户联系人信息
    addContact2BlackList: "/api/userContact/blacklistUserContact",// 加入黑名单
    delContact: "/api/userContact/delUserContact",// 删除好友

    // ========== 用户信息相关 (user-info-manager) ==========
    saveUserInfo: "/api/userInfo/updateUserInfo",// 修改用户信息
    updatePassword: "/api/userInfo/updateUserPassword",// 修改密码
    logout: "/api/userInfo/logout",// 退出登录
    
    // ========== 聊天消息相关 (chat-message-manager) ==========
    sendMessage: "/api/chat/sendMessage",// 发送消息
    deleteMessage: "/api/chat/deleteMessage",// 删除消息
    uploadFile: "/api/chat/uploadFile",// 上传文件

    // ========== AI助手相关 (chat-agent) ==========
    sendMessageToAI: "/api/agent/chat",// 发送消息给AI
    getMessageList: "/api/agent/history",// 获取消息列表
    generateAiSessionTitle: "/api/agent/getTitle",// 生成AI会话标题

    // ========== 版本更新 (user-info-manager) ==========
    checkVersion: "/api/systemVersion/checkVersion", // 检查版本
    
    // ========== 管理员功能 (user-info-manager) ==========
    loadAdminAccount: "/api/admin/loadUser", //加载用户列表
    loadBeautyAccountList: "/api/admin/loadBeautyAccountList", //加载用户列表
    saveBeautyAccount: "/api/admin/saveBeautyAccount", //保存靓号
    changeAccountStatus: "/api/admin/updateUserStatus", //修改用户状态
    forceOffLine: "/api/admin/forceOffLine", //强制下线
    delBeautyAccount: "/api/admin/delBeautyAccount", //删除靓号
    loadGroup: "/api/admin/loadGroup",// 加载群聊
    adminDissolutionGroup: "/api/admin/dissolutionGroup",// 解散群聊
    getSysSetting: "/api/admin/getSysSetting",// 获取系统设置
    saveSysSetting: "/api/admin/saveSysSetting",// 保存系统设置
    loadUpdateList: "/api/admin/loadUpdateList",// 加载更新列表
    saveUpdate: "/api/admin/saveUpdate",// 保存更新
    delUpdate: "/api/admin/delUpdate",// 删除更新
}

export default api