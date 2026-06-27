<template>
  <div class="login-wrapper">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="bg-circle circle-1"></div>
      <div class="bg-circle circle-2"></div>
      <div class="bg-circle circle-3"></div>
      <div class="bg-blob"></div>
    </div>

    <div v-if="showLoading" class="loading-overlay">
      <div class="loading-content">
        <div class="loading-spinner"></div>
        <p>正在登录...</p>
      </div>
    </div>

    <div class="login-card">
      <div class="card-header drag">
        <div class="logo-area">
          <div class="logo-icon">
            <span class="iconfont icon-chat"></span>
          </div>
          <h1 class="app-title">Chat</h1>
        </div>
        <p class="app-subtitle">{{ isLogin ? '欢迎回来，请登录您的账号' : '创建新账号，开始您的旅程' }}</p>
      </div>

      <div class="card-body">
        <div class="error-msg" v-if="errorMessage">
          <span class="error-icon">!</span>
          {{ errorMessage }}
        </div>

        <el-form :model="formData" ref="formDataRef" @submit.prevent>
          <!-- 邮箱输入 -->
          <el-form-item prop="email">
            <div class="email-panel">
              <el-input size="large" clearable placeholder="请输入邮箱地址" v-model.trim="formData.email" maxlength="30"
                @focus="cleanVerify" class="custom-input">
                <template #prefix>
                  <span class="input-icon iconfont icon-email"></span>
                </template>
              </el-input>
              <el-dropdown v-if="isLogin && localUserList?.length > 0" trigger="click">
                <span class="dropdown-trigger iconfont icon-down"></span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-for="item in localUserList" :key="item.userId">
                      <div class="email-select" @click="selectLocalUser(item.email)">{{ item.email }}</div>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </el-form-item>

          <!-- 昵称输入 -->
          <el-form-item prop="nickname" v-if="!isLogin">
            <el-input size="large" clearable placeholder="给自己取一个昵称" v-model.trim="formData.nickname" maxlength="15"
              @focus="cleanVerify" class="custom-input">
              <template #prefix>
                <span class="input-icon iconfont icon-user-nick"></span>
              </template>
            </el-input>
          </el-form-item>

          <!-- 密码输入 -->
          <el-form-item prop="password">
            <el-input size="large" show-password clearable placeholder="请输入密码" v-model.trim="formData.password"
              type="password" @focus="cleanVerify" class="custom-input">
              <template #prefix>
                <span class="input-icon iconfont icon-password"></span>
              </template>
            </el-input>
          </el-form-item>

          <!-- 确认密码输入 -->
          <el-form-item prop="confirmPassword" v-if="!isLogin">
            <el-input size="large" show-password clearable placeholder="请再次输入密码" v-model.trim="formData.confirmPassword"
              type="password" @focus="cleanVerify" class="custom-input">
              <template #prefix>
                <span class="input-icon iconfont icon-password"></span>
              </template>
            </el-input>
          </el-form-item>

          <!-- 验证码输入 -->
          <el-form-item prop="checkCode">
            <div class="check-code-panel">
              <el-input size="large" clearable placeholder="请输入验证码" v-model.trim="formData.checkCode" @focus="cleanVerify"
                @keydown.enter="handleLogin" class="custom-input check-input">
                <template #prefix>
                  <span class="input-icon iconfont icon-checkcode"></span>
                </template>
              </el-input>
              <div class="captcha-box" @click="changeCheckCode">
                <img :src="checkCodeUrl" class="check-code-img" alt="验证码">
                <span class="refresh-hint">点击刷新</span>
              </div>
            </div>
          </el-form-item>

          <!-- 登录按钮 -->
          <el-form-item>
            <button type="button" class="submit-btn" @click="handleLogin">
              <span>{{ isLogin ? '登 录' : '注 册' }}</span>
              <svg class="btn-arrow" viewBox="0 0 24 24" fill="none">
                <path d="M5 12h14m-7-7 7 7-7 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </el-form-item>

          <div class="bottom-link">
            <span class="a-link" @click="changeState">
              {{ isLogin ? '还没有账号？立即注册' : '已有账号？立即登录' }}
            </span>
          </div>

        </el-form>
      </div>
    </div>

    <win-o-p :show-set-top="false" :show-max="false" :show-min="false" :close-type="0" :dark-bg="true"></win-o-p>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, onMounted, onUnmounted } from "vue";
const { proxy } = getCurrentInstance();
import md5 from "js-md5";
import { useUserInfoStore } from "../stores/UserInfoStore";
import { useRouter } from "vue-router";
import WinOP from "../components/WinOP.vue";
const userInfoStore = useUserInfoStore()
const router = useRouter()

const showLoading = ref(false)

const getInitialFormData = () => ({
  email: '',
  password: '',
  checkCode: '',
  nickname: '',
  confirmPassword: ''
})

const formData = ref(getInitialFormData())

// 表单的 DOM 引用
const formDataRef = ref()

const isLogin = ref(true)

const errorMessage = ref('')

// 获取验证码
const checkCodeUrl = ref(null)
const changeCheckCode = async () => {
  let result = await proxy.Request({
    url: proxy.Api.checkCode,
    method: 'get',
    params: {
    }
  })
  if (!result) {
    return;
  }
  checkCodeUrl.value = result.data.checkCode;
  localStorage.setItem('checkCodeKey', result.data.checkCodeKey)
}

changeCheckCode()
// 登录按钮点击事件
const handleLogin = async () => {
  cleanVerify()
  if (!checkValue('checkEmail', formData.value.email, '请输入正确的邮箱地址')) {
    return
  }
  if (!isLogin.value && !checkValue(null, formData.value.nickname, '请输入昵称')) {
    return
  }
  if (!checkValue('checkPassword', formData.value.password, '密码只能是8-18位的数字、字母或下划线')) {
    return
  }
  if (!isLogin.value && formData.value.password !== formData.value.confirmPassword) {
    errorMessage.value = '两次输入的密码不一致'
    return
  }
  if (!checkValue(null, formData.value.checkCode, '验证码错误')) {
    return
  }

  if (isLogin.value) {
    showLoading.value = true
  }
  let result = await proxy.Request({
    url: isLogin.value ? proxy.Api.login : proxy.Api.register,
    showLoading: isLogin.value ? true : false,
    method: 'post',
    showError: false,
    params: {
      email: formData.value.email,
      password: md5(formData.value.password),
      nickname: formData.value.nickname,
      checkCode: formData.value.checkCode,
      checkCodeKey: localStorage.getItem('checkCodeKey')
    },
    errorCallback: (response) => {
      changeCheckCode()
      showLoading.value = false
      errorMessage.value = response.info
    },
  })

  if (!result) {
    showLoading.value = false
    return
  }
  if (isLogin.value) {
    userInfoStore.setUserInfo(result.data)
    localStorage.setItem('token', result.data.token)
    window.ipcRenderer.send('setLocalStore', { key: "devDomain", value: proxy.Api.devDomain })
    const screenWidth = window.screen.width
    const screenHeight = window.screen.height

    window.ipcRenderer.once('loginSuccessCallBack', async () => {
      await router.replace('/main')
      showLoading.value = false
    })

    window.ipcRenderer.send('loginSuccess',
      {
        userId: result.data.userId,
        nickname: result.data.nickname,
        token: result.data.token,
        email: formData.value.email,
        admin: result.data.admin,
        screenHeight: screenHeight,
        screenWidth: screenWidth
      }
    )
    return

  } else {
    proxy.Message.success('注册成功，请登录')
    changeState()
  }
  if (showLoading.value) {
    showLoading.value = false
  }
}


const cleanVerify = () => {
  errorMessage.value = ''
}

const checkValue = (type, value, msg) => {
  if (proxy.Utils.isEmpty(value)) {
    errorMessage.value = msg
    return false
  }
  if (type !== null && !proxy.Verify[type](value)) {
    errorMessage.value = msg
    return false
  }

  return true
}

const changeState = () => {
  window.ipcRenderer.send('loginOrRegister', !isLogin.value)
  isLogin.value = !isLogin.value
  nextTick(() => {
    formData.value = getInitialFormData()
    formDataRef.value.resetFields()
    cleanVerify()
    changeCheckCode()
  })
}

const init = () => {
  window.ipcRenderer.send('setLocalStore', { key: "devDomain", value: proxy.Api.devDomain })
  window.ipcRenderer.send('setLocalStore', { key: "prodDomain", value: proxy.Api.prodDomain })
  window.ipcRenderer.send('setLocalStore', { key: "prodWsDomain", value: proxy.Api.prodWsDomain })
  window.ipcRenderer.send('setLocalStore', { key: "devWsDomain", value: proxy.Api.devWsDomain })
  window.ipcRenderer.send('loadLocalUser');
  window.ipcRenderer.on('loadLocalUserCallBack', (event, userList) => {
    localUserList.value = userList
  })
}

//本地用户列表
const localUserList = ref()

const selectLocalUser = (email) => {
  formData.value.email = email
}

onMounted(() => {
  init()
})

onUnmounted(() => {
  window.ipcRenderer.removeAllListeners('loadLocalUserCallBack')
})

</script>

<style lang="scss" scoped>
.email-select {
  width: 250px;
}

/* ===== 登录页容器 ===== */
.login-wrapper {
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

/* ===== 背景装饰 ===== */
.bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.15;
  
  &.circle-1 {
    width: 400px;
    height: 400px;
    background: #fff;
    top: -100px;
    right: -80px;
    animation: floatCircle 8s ease-in-out infinite;
  }
  
  &.circle-2 {
    width: 250px;
    height: 250px;
    background: #a78bfa;
    bottom: -60px;
    left: -40px;
    animation: floatCircle 6s ease-in-out infinite reverse;
  }
  
  &.circle-3 {
    width: 150px;
    height: 150px;
    background: #c4b5fd;
    top: 40%;
    left: 10%;
    animation: floatCircle 10s ease-in-out infinite 2s;
  }
}

.bg-blob {
  position: absolute;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(255,255,255,0.08) 0%, transparent 70%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  border-radius: 50%;
  animation: pulseBlob 4s ease-in-out infinite;
}

@keyframes floatCircle {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-30px) scale(1.05); }
}

@keyframes pulseBlob {
  0%, 100% { transform: translate(-50%, -50%) scale(1); opacity: 0.5; }
  50% { transform: translate(-50%, -50%) scale(1.3); opacity: 0.2; }
}

/* ===== 加载遮罩 ===== */
.loading-overlay {
  position: absolute;
  inset: 0;
  background: rgba(30, 27, 75, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  backdrop-filter: blur(10px);
}

.loading-content {
  text-align: center;

  p {
    color: #c4b5fd;
    margin-top: 20px;
    font-size: 15px;
    letter-spacing: 2px;
  }
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 3px solid rgba(255, 255, 255, 0.15);
  border-top-color: #a78bfa;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ===== 登录卡片 ===== */
.login-card {
  position: relative;
  z-index: 1;
  width: 380px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(20px) saturate(180%);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 
    0 25px 50px rgba(0, 0, 0, 0.15),
    0 0 0 1px rgba(255, 255, 255, 0.1) inset;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

/* ===== 卡片头部 ===== */
.card-header {
  padding: 35px 30px 20px;
  text-align: center;

  .logo-area {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    margin-bottom: 12px;
  }

  .logo-icon {
    width: 46px;
    height: 46px;
    border-radius: 14px;
    background: linear-gradient(135deg, #6366f1, #8b5cf6);
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 8px 20px rgba(99, 102, 241, 0.35);

    .iconfont {
      font-size: 26px;
      color: #fff;
    }
  }

  .app-title {
    font-size: 26px;
    font-weight: 700;
    color: #1f2937;
    margin: 0;
    letter-spacing: -0.5px;
  }

  .app-subtitle {
    font-size: 13px;
    color: #9ca3af;
    margin: 0;
    letter-spacing: 0.3px;
  }
}

/* ===== 卡片主体 ===== */
.card-body {
  padding: 0 30px 30px;
}

/* ===== 错误提示 ===== */
.error-msg {
  background: linear-gradient(135deg, #fef2f2, #fee2e2);
  color: #dc2626;
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 13px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  border: 1px solid #fecaca;
  animation: shakeIn 0.4s ease;

  .error-icon {
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background: #ef4444;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    font-weight: 700;
    flex-shrink: 0;
  }
}

@keyframes shakeIn {
  0% { transform: translateX(-8px); opacity: 0; }
  25% { transform: translateX(6px); }
  50% { transform: translateX(-4px); }
  75% { transform: translateX(2px); }
  100% { transform: translateX(0); opacity: 1; }
}

/* ===== 自定义输入框 ===== */
:deep(.custom-input) {
  .el-input__wrapper {
    background: #f8f7ff;
    border-radius: 12px;
    border: 2px solid transparent;
    box-shadow: none;
    padding-left: 12px;
    padding-right: 12px;
    height: 46px;
    transition: all 0.25s ease;

    &:hover {
      background: #f0eeff;
      border-color: #c4b5fd;
    }

    &.is-focus {
      background: #fff;
      border-color: #6366f1;
      box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.1);
    }
  }

  .el-input__inner {
    color: #1f2937;
    font-size: 14px;

    &::placeholder {
      color: #c4b5fd;
    }
  }

  .el-input__suffix {
    color: #a78bfa;
  }
}

.input-icon {
  color: #a78bfa;
  font-size: 18px;
}

/* ===== 表单项 ===== */
:deep(.el-form-item) {
  margin-bottom: 16px;

  .el-form-item__content {
    width: 100%;
  }
}

/* ===== 邮箱面板 ===== */
.email-panel {
  display: flex;
  align-items: center;
  gap: 6px;

  .el-input {
    flex: 1;
  }

  .dropdown-trigger {
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f0eeff;
    border-radius: 10px;
    cursor: pointer;
    color: #6366f1;
    font-size: 14px;
    transition: all 0.2s ease;

    &:hover {
      background: #ddd6fe;
      transform: scale(1.05);
    }
  }
}

/* ===== 验证码面板 ===== */
.check-code-panel {
  display: flex;
  gap: 10px;
  align-items: center;

  .check-input {
    flex: 1;
  }

  .captcha-box {
    flex-shrink: 0;
    cursor: pointer;
    border-radius: 12px;
    overflow: hidden;
    border: 2px solid #e5e7eb;
    transition: all 0.25s ease;
    position: relative;

    &:hover {
      border-color: #a78bfa;
      box-shadow: 0 0 0 4px rgba(139, 92, 246, 0.1);

      .refresh-hint {
        opacity: 1;
      }
    }

    .check-code-img {
      display: block;
      height: 46px;
      width: auto;
    }

    .refresh-hint {
      position: absolute;
      inset: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(30, 27, 75, 0.7);
      color: #fff;
      font-size: 12px;
      opacity: 0;
      transition: opacity 0.2s ease;
      pointer-events: none;
    }
  }
}

/* ===== 提交按钮 ===== */
.submit-btn {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 14px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  cursor: pointer;
  margin-top: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  box-shadow: 0 8px 25px rgba(99, 102, 241, 0.35);

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, #8b5cf6, #6366f1);
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 12px 35px rgba(99, 102, 241, 0.45);

    &::before {
      opacity: 1;
    }

    .btn-arrow {
      transform: translateX(4px);
    }
  }

  &:active {
    transform: translateY(0);
    box-shadow: 0 4px 15px rgba(99, 102, 241, 0.35);
  }

  span {
    position: relative;
    z-index: 1;
  }
}

.btn-arrow {
  width: 18px;
  height: 18px;
  position: relative;
  z-index: 1;
  transition: transform 0.3s ease;
}

/* ===== 底部链接 ===== */
.bottom-link {
  text-align: center;
  margin-top: 6px;

  .a-link {
    font-size: 13px;
    color: #8b5cf6;
    cursor: pointer;
    transition: color 0.2s ease;

    &:hover {
      color: #6366f1;
    }
  }
}
</style>
