import { createApp } from 'vue'

import ElementPlus from 'element-plus'
import App from './App.vue'
import router from '@/router'
import { createPinia } from "pinia"

import 'element-plus/dist/index.css'
import '@/assets/cust-elementplus.scss'
import '@/assets/icon/iconfont.css'
import '@/assets/base.scss'

import Layout from '@/components/Layout.vue'
import WinOP from '@/components/WinOP.vue'
import ContentPanel from '@/views/contact/ContentPanel.vue'
import ShowLocalImage from '@/components/ShowLocalImage.vue'
import UserBaseInfo from '@/components/UserBaseInfo.vue'
import Dialogue from './components/Dialogue.vue'
import Avatar from '@/components/Avatar.vue'
import AvatarUpload from '@/components/AvatarUpload.vue'
import Badge from '@/components/Badge.vue'
import Table from '@/components/Table.vue'


import Utils from '@/utils/utils'
import Verify from '@/utils/verify'
import Request from '@/utils/request'
import Api from '@/utils/api'
import Message from '@/utils/message'
import Confirm from '@/utils/confirm'

const app = createApp(App)
app.use(createPinia())
app.use(ElementPlus)
app.use(router)
app.component('Layout', Layout)
app.component('WinOP', WinOP)
app.component('ContentPanel', ContentPanel)
app.component('ShowLocalImage', ShowLocalImage)
app.component('UserBaseInfo', UserBaseInfo)
app.component('Dialogue', Dialogue)
app.component('Avatar', Avatar)
app.component('AvatarUpload', AvatarUpload)
app.component('Badge', Badge)
app.component('Table', Table)

app.config.globalProperties.Utils = Utils
app.config.globalProperties.Verify = Verify
app.config.globalProperties.Api = Api
app.config.globalProperties.Request = Request
app.config.globalProperties.Message = Message
app.config.globalProperties.Confirm = Confirm

app.mount('#app')