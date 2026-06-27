<template>
  <div class="top-panel">
    <el-card>
      <el-form :model="searchForm" label-width="70px" label-position="right">
        <el-row>
          <el-col :span="5">
            <el-form-item label="ID" label-width="40px">
              <el-input class="password-input" v-model="searchForm.userIdFuzzy" clearable
                @keyup.native="loadDataList"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="昵称">
              <el-input class="password-input" v-model="searchForm.nickNameFuzzy" clearable
                @keyup.native="loadDataList"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="4" :style="{ paddingLeft: '10px' }">
            <el-button type="success" @click="loadDataList">查询</el-button>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
    <el-card class="table-data-card">
      <Table :columns="columns" :fetch="loadDataList" :dataSource="tableData" :options="tableOptions">
        <template #slotAvatar="{ index, row }">
          <AvatarBase :width="50" :userId="row.userId" partType="avatar"></AvatarBase>
        </template>
        <template #slotNickName="{ index, row }">
          {{ row.nickname }}
          ({{ row.userId }})
          <span v-if="row.sex === 1" class="iconfont icon-woman"></span>
          <span v-else class="iconfont icon-man"></span>
        </template>
        <template #slotStatus="{ index, row }">
          <el-tag v-if="row.status === 0" style="color: green;">正常</el-tag>
          <el-tag v-else style="color: red;">禁用</el-tag>
        </template>
        <template #slotOnline="{ index, row }">
          <el-tag v-if="row.onlineType" style="color: green;">在线</el-tag>
          <el-tag v-else style="color: #8a8a8a;">离线</el-tag>
        </template>
        <template #slotOperation="{ index, row }">
          <el-dropdown placement="bottom-end" trigger="click" v-if="userInfoStore.getInfo().userId !== row.userId">
            <span class="iconfont icon-more"></span>
            <template #dropdown>
              <el-dropdown-item @click="changeAccountStatus(row)">{{ row.status === 1 ? "启用" : "禁用"
                }}</el-dropdown-item>
              <el-dropdown-item @click="forceOffLine(row)" v-if="row.onlineType">强制下线</el-dropdown-item>
            </template>
          </el-dropdown>
          <div v-else>管理员</div>
        </template>
      </Table>
    </el-card>
  </div>
</template>

<script setup>
import AvatarBase from "@/components/AvatarBase.vue";
import { ref, getCurrentInstance, onMounted } from "vue";
const { proxy } = getCurrentInstance();

import { useUserInfoStore } from "@/stores/UserInfoStore";
const userInfoStore = useUserInfoStore();

const tableData = ref({});
const tableOptions = {};
const columns = [
  {
    label: "头像",
    prop: "useId",
    width: 70,
    scopedSlots: "slotAvatar"
  },
  {
    label: "昵称",
    prop: "nickName",
    scopedSlots: "slotNickName"
  },
  {
    label: "邮箱",
    prop: "email",
    width: 200
  },
  {
    label: "加入时间",
    prop: "createTime",
    width: 200
  },
  {
    label: "地区",
    prop: "areaName",
    width: 150
  },
  {
    label: "用户状态",
    prop: "status",
    width: 100,
    scopedSlots: "slotStatus"
  },
  {
    label: "在线状态",
    prop: "onlineType",
    width: 100,
    scopedSlots: "slotOnline"
  },
  {
    label: "操作",
    prop: "operation",
    width: 100,
    scopedSlots: "slotOperation"
  }
]

const searchForm = ref({});

const loadDataList = async () => {
  let params = {
    pageNum: tableData.value.pageNum,
    pageSize: tableData.value.pageSize
  };
  Object.assign(params, searchForm.value);
  let result = await proxy.Request({
    url: proxy.Api.loadAdminAccount,
    method: "get",
    params: params
  })
  if (!result) {
    return;
  }
  Object.assign(tableData.value, result.data);
}

const changeAccountStatus = async (row) => {
  let status = row.status === 0 ? 1 : 0;
  let info = status === 0 ? "正常" : "禁用";
  proxy.Confirm({
    message: `确定将用户【${row.nickname}】设置为【${info}】吗？`,
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.Api.changeAccountStatus,
        method: "put",
        params: {
          userId: row.userId,
          status: status
        }
      })
      if (!result) {
        return;
      }
      proxy.Message.success("操作成功")
      loadDataList();
    }
  })
}

const forceOffLine = async (row) => {
  proxy.Confirm({
    message: `确定将用户【${row.nickname}】强制下线吗？`,
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.Api.forceOffLine,
        method: "put",
        params: {
          userId: row.userId
        }
      })
      if (!result) {
        return;
      }
      proxy.Message.success("操作成功")
      loadDataList();
    }
  })
}
const getUserInfo = async () => {
  let result = await proxy.Request({
    url: proxy.Api.getUserInfo,
    method: "get"
  })
  if (!result) {
    return;
  }
  userInfoStore.setUserInfo(result.data);
}

onMounted(() => {
  getUserInfo();
})

</script>

<style lang="scss" scoped>
.icon-main {
  color: #2cb6fe;
}

.icon-woman {
  color: #fb7373;
}
</style>
