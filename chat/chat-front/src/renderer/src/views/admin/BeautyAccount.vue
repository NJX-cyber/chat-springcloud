<template>
  <div class="top-panel">
    <el-card>
      <el-form :model="searchForm" label-width="70px" label-position="right">
        <el-row>
          <el-col :span="5">
            <el-form-item label="靓号" label-width="40px">
              <el-input class="password-input" v-model="searchForm.userIdFuzzy" clearable
                @keyup.native="loadDataList"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="邮箱">
              <el-input class="password-input" v-model="searchForm.emailFuzzy" clearable
                @keyup.native="loadDataList"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="4" :style="{ paddingLeft: '10px' }">
            <el-button type="success" @click="loadDataList">查询</el-button>
            <el-button type="primary" @click="editAccount">新增靓号</el-button>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
    <el-card class="table-data-card">
      <Table :columns="columns" :fetch="loadDataList" :dataSource="tableData" :options="tableOptions">
        <template #slotStatus="{ index, row }">
          <el-tag v-if="!row.status || row.status === 0" style="color: green;">未使用</el-tag>
          <el-tag v-else style="color: red;">已使用</el-tag>
        </template>
        <template #slotOperation="{ index, row }">
          <el-dropdown placement="bottom-end" trigger="click">
            <span class="iconfont icon-more"></span>
            <template #dropdown>
              <el-dropdown-item @click="editAccount(row)" v-if="!row.status || row.status == 0">修改</el-dropdown-item>
              <el-dropdown-item @click="delAccount(row)">删除</el-dropdown-item>
            </template>
          </el-dropdown>
        </template>
      </Table>
    </el-card>
    <BeautyAccountEdit ref="beautyAccountEditRef" @reload="loadDataList"></BeautyAccountEdit>
  </div>
</template>

<script setup>
import BeautyAccountEdit from "./BeautyAccountEdit.vue"
import { ref, getCurrentInstance, onMounted } from "vue";
const { proxy } = getCurrentInstance();

import { useUserInfoStore } from "@/stores/UserInfoStore";
const userInfoStore = useUserInfoStore();

const tableData = ref({});
const tableOptions = {};
const columns = [
  {
    label: "邮箱",
    prop: "email"
  },
  {
    label: "靓号",
    prop: "userId"
  },
  {
    label: "状态",
    prop: "status",
    scopedSlots: "slotStatus"
  },
  {
    label: "操作",
    prop: "operation",
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
    url: proxy.Api.loadBeautyAccountList,
    method: "get",
    params: params
  })
  if (!result) {
    return;
  }
  Object.assign(tableData.value, result.data);
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

const beautyAccountEditRef = ref();
const editAccount = (row = {}) => {
  beautyAccountEditRef.value.showEdit(row);
}

const delAccount = async (row) => {
  let result = await proxy.Request({
    url: proxy.Api.delBeautyAccount,
    method: "delete",
    params: {
      id: row.id
    }
  })
  if (!result) {
    return;
  }
  proxy.Message.success("删除成功");
  loadDataList();
}

onMounted(() => {
  getUserInfo();
})

</script>

<style lang="scss" scoped></style>
