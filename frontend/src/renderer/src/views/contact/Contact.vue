<template>
  <Layout>
    <template #left-content>
      <div class="drag-panel drag"></div>
      <div class="top-search">
        <el-input clearable placeholder="搜索" v-model="searchValue" size="small" @keyup="search">
          <template #suffix>
            <span class="iconfont icon-search"></span>
          </template>
        </el-input>
      </div>
      <div class="contact-list" v-show="!searchValue">
        <template v-for="item in partList" :key="item.partName">
          <div class="part-title">{{ item.partName }}</div>
          <div class="part-list">
            <div v-for="sub in item.children" :key="sub.name"
              :class="['part-item', sub.path === route.path ? 'activate' : '']" @click="partJump(sub)">
              <div :class="['iconfont', sub.icon]" :style="{ background: sub.iconBgColor }"></div>
              <div class="text">{{ sub.name }}</div>
              <Badge :count="messageCountStore.getCount(sub.countKey)" :top="3" :left="45"></Badge>
            </div>
            <template v-for="contact in item.contactData">
              <div :class="['part-item', contact[item.contactId] === route.query.id ? 'activate' : '']"
                @click="contactDetail(contact, item)">
                <Avatar :userId="contact[item.contactId]" :width="32"></Avatar>
                <div class="text">{{ contact[item.contactName] }}</div>
              </div>
            </template>
            <template v-if="item.contactData && item.contactData.length === 0">
              <div class="no-data">{{ item.emptyMsg }}</div>
            </template>
          </div>
        </template>
      </div>
      <div class="search-list" v-show="searchValue"> 
        <ContactSearchResult :data="item" v-for="item in searchList" :key="item.contactId" 
        @click="searchClickHandler(item)"></ContactSearchResult>
      </div>
    </template>
    <template #right-content>
      <div class="title-panel drag">{{ rightTitle }}</div>
      <RouterView v-slot="{ Component }">
        <component :is="Component" ref="componentRef" />
      </RouterView>
    </template>
  </Layout>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, watch, onMounted } from "vue"
import { useRoute, useRouter } from 'vue-router'
import { useContactStateStore } from "@/stores/ContactStateStore";
import { useMessageCountStore } from "@/stores/MessageCountStore";
import ContactSearchResult from "./ContactSearchResult.vue";

const contactStateStore = useContactStateStore();
const messageCountStore = useMessageCountStore();
const { proxy } = getCurrentInstance();
const route = useRoute();
const router = useRouter()

const partList = ref([
  {
    partName: "新朋友",
    children: [
      {
        name: "搜好友",
        icon: 'icon-search',
        iconBgColor: "#fa9d3b",
        path: "/contact/Search"
      },
      {
        name: "新的朋友",
        icon: "icon-plane",
        iconBgColor: "#8b5cf6",
        path: "/contact/contactApply",
        showTitle: true,
        partName: "新的朋友",
        countKey: "contactApplyCount"
      }
    ]
  },
  {
    partName: "我的群聊",
    children: [
      {
        name: "新建群聊",
        icon: "icon-add-group",
        iconBgColor: "#1485ee",
        path: "/contact/createGroup"
      }
    ],
    contactId: "contactId",
    contactName: "contactName",
    showTitle: true,
    contactData: [],
    contactPath: "/contact/groupDetail",
  },
  {
    partName: "我加入的群聊",
    contactId: "contactId",
    contactName: "contactName",
    showTitle: true,
    contactData: [],
    contactPath: "/contact/groupDetail",
    emptyMsg: "暂无群聊"
  },
  {
    partName: "我的好友",
    contactId: "contactId",
    contactName: "contactName",
    children: [],
    contactData: [],
    contactPath: "/contact/userDetail",
    emptyMsg: "暂无好友"
  }
])

const partJump = ((item) => {
  if (item.showTitle) {
    rightTitle.value = item.partName
  } else {
    rightTitle.value = ""
  }
  if (item.countKey) {
    messageCountStore.setCount(item.countKey, 0, true)
    window.ipcRenderer.send("updateContactNoReadCount")
  }
  router.push(item.path)
})

const rightTitle = ref("")

const loadContact = async (contactType) => {
  let result = await proxy.Request({
    url: proxy.Api.loadContact,
    method: 'get',
    params: {
      contactType: contactType === "GROUP" ? 1 : 0
    }
  })
  if (!result) {
    return;
  }
  if (contactType === "GROUP") {
    partList.value[2].contactData = result.data
  } else if (contactType === "USER") {
    partList.value[3].contactData = result.data
  }
}

const loadMyGroup = async () => {
  let result = await proxy.Request({
    url: proxy.Api.loadMyGroup,
    method: 'get',
    showLoading: false
  })
  if (!result) {
    return;
  }
  partList.value[1].contactData = result.data
  partList.value[1].contactData.forEach(item => {
    item.contactId = item.groupId
    item.contactName = item.groupName
  })
}

const contactDetail = (contact, item) => {
  if (item.showTitle) {
    rightTitle.value = contact[item.contactName]
  } else {
    rightTitle.value = ""
  }
  router.push({
    path: item.contactPath,
    query: {
      contactId: contact[item.contactId]
    }
  })
}

const searchValue = ref();
const searchList = ref([]);
const search = () => {
  if (!searchValue.value) {
    return;
  }
  searchList.value = [];
  const regex = new RegExp("(" + searchValue.value + ")", "gi");
  let allContactList = [];
  partList.value.forEach(item => {
    if (item.contactData) {
      allContactList = allContactList.concat(item.contactData);
    }
  });
  allContactList.forEach(item => {
    let contactName = item.groupId ? item.groupName : item.contactName;
    if (item.contactName.includes(searchValue.value)) {
      let newData = Object.assign({}, item);
      newData.searchContactName = item.contactName.replace(regex, "<span class='highlight'>$1</span>");
      newData.contactId = item.groupId || item.contactId;
      searchList.value.push(newData);
    }
  });
};

const searchClickHandler = (item) => {
  // searchValue.value = undefined;
  // searchList.value = [];
  router.push({
    path: "/chat",
    query: {
      contactId: item.contactId,
      timeStamp: Date.now(),
    }
  });
};

onMounted(() => {
  loadMyGroup();
  loadContact("GROUP");
  loadContact("USER");
});

watch(() => contactStateStore.contactReload, (newValue, oldValue) => {
  if (!newValue) {
    return;
  }
  switch (newValue) {
    case "GROUP":
      loadContact("GROUP");
      break;
    case "USER":
      loadContact("USER");
      break;
    case "MY_GROUP":
      loadMyGroup();
      break;
    case "REMOVE_CONTACT":
      rightTitle.value = null;
      router.push("/contact/blankPage");
      loadContact("USER");
      break;
    case "LEAVE_GROUP":
      rightTitle.value = null;
      router.push("/contact/blankPage");
      loadContact("GROUP");
      break;
    case "DISSOLUTION_GROUP":
      rightTitle.value = null;
      router.push("/contact/blankPage");
      loadMyGroup();
      break;
    default:
      break;
  }
},
  { immediate: true, deep: true }
)

</script>

<style lang="scss" scoped>
.drag-panel {
  height: 25px;
  background: #f7f7f7;
}

.top-search {
  padding: 0px 10px 9px 10px;
  background: #f7f7f7;
  display: flex;
  align-items: center;

  .iconfont {
    font-size: 12px;
  }
}

.contact-list {
  border-top: 1px solid #ddd;
  height: calc(100vh - 62px);
  overflow: hidden;

  &:hover {
    overflow-y: auto;
  }

  .part-title {
    color: #515151;
    padding-left: 10px;
    margin-top: 10px;
  }

  .part-list {
    border-bottom: 1px solid #ddd;

    .part-item {
      display: flex;
      align-items: center;
      padding: 10px 10px;
      position: relative;

      &:hover {
        cursor: pointer;
        background: #d6d6d6;
      }

      .iconfont {
        width: 35px;
        height: 35px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 20px;
        color: #fff;
      }

      .text {
        flex: 1;
        color: #000000;
        margin-left: 10px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }

    .no-data {
      text-align: center;
      font-size: 12px;
      color: #9d9d9d;
      line-height: 30px;
    }

    .activate {
      background: #c4c4c4;

      &:hover {
        background: #c4c4c4;
      }
    }
  }
}

.title-panel {
  width: 100%;
  height: 58.2px;
  display: flex;
  align-items: center;
  padding-left: 10px;
  font-size: 18px;
  color: #000000;
}
</style>