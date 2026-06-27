<template>
  <div class="top-panel">
    <el-card>
      <el-form :model="searchForm" label-width="70px" label-position="right">
        <el-row>
          <el-col :span="5">
            <el-form-item label="发布日期" label-width="70px">
              <el-date-picker v-model="searchFormData.createTimeRange" type="daterange" range-separator="至"
                start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" @change="loadDataList" />
            </el-form-item>
          </el-col>
          <el-col :span="4" :style="{ paddingLeft: '10px' }">
            <el-button type="success" @click="loadDataList">查询</el-button>
            <el-button type="primary" @click="showEdit">新增版本</el-button>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
    <el-card class="table-data-card">
      <Table :columns="columns" :fetch="loadDataList" :dataSource="tableData" :options="tableOptions">
        <template #slotUpdateDesc="{ index, row }">
          <div v-for="(item, index) in row.updateDescArray">{{ index + 1 }}、{{ item }}</div>
        </template>
        <template #slotFileType="{ index, row }">
          <div v-if="row.fileType === 0">本地文件</div>
          <div v-if="row.fileType === 1">{{ row.outLink }}</div>
        </template>
        <template #slotStatus="{ index, row }">
          <el-tag v-if="row.status === 0" style="color: #f56c6c;">未发布</el-tag>
          <el-tag v-if="row.status === 1" style="color: #f56c6c;">灰度发布</el-tag>
          <el-tag v-if="row.status === 2" style="color: #f56c6c;">全网发布</el-tag>
        </template>
        <template #slotOperation="{ index, row }">
          <el-dropdown placement="bottom-end" trigger="click">
            <span class="iconfont icon-more"></span>
            <template #dropdown>
              <el-dropdown-item @click="showEdit(row)" v-if="row.status == 0">修改</el-dropdown-item>
              <el-dropdown-item @click="updatePost(row)">发布</el-dropdown-item>
              <el-dropdown-item @click="del(row)" v-if="row.status == 0">删除</el-dropdown-item>
            </template>
          </el-dropdown>
        </template>
      </Table>
    </el-card>
    <UpdateEdit ref="updateEditRef" @reload="loadDataList"></UpdateEdit>
    <UpdatePost ref="updatePostRef" @reload="loadDataList"></UpdatePost>
  </div>
</template>

<script setup>
import UpdateEdit from "./UpdateEdit.vue";
import UpdatePost from "./UpdatePost.vue";
import { ref, getCurrentInstance, onMounted } from "vue";
const { proxy } = getCurrentInstance();

const searchFormData = ref({
  createTimeRange: []
});
const tableData = ref({});
const tableOptions = {};
const columns = [
  {
    label: "版本",
    prop: "version",
    width: 200,
  },
  {
    label: "更新内容",
    prop: "updateDesc",
    scopedSlots: "slotUpdateDesc",
    width: 300
  },
  {
    label: "发布时间",
    prop: "createTime",
    width: 200
  },
  {
    label: "文件类型",
    prop: "fileType",
    scopedSlots: "slotFileType"
  },
  {
    label: "状态",
    prop: "status",
    width: 100,
    scopedSlots: "slotStatus"
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
  if (searchFormData.value.createTimeRange) {
    params.createTimeStart = searchFormData.value.createTimeRange[0];
    params.createTimeEnd = searchFormData.value.createTimeRange[1];
  }
  Object.assign(params, searchForm.value);
  let result = await proxy.Request({
    url: proxy.Api.loadUpdateList,
    method: "get",
    params: params
  })
  if (!result) {
    return;
  }
  tableData.value = result.data;
}

const updateEditRef = ref(null);
const showEdit = (row) => {
  updateEditRef.value.showEdit(row);
}

const updatePostRef = ref(null);
const updatePost = (row) => {
  updatePostRef.value.showEdit(row);
}

const del = (row) => {
  proxy.Confirm({
    message: `确定删除【${row.version}】吗？`,
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.Api.delUpdate,
        method: "delete",
        params: {
          userId: row.Id
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

</script>

<style lang="scss" scoped>
.form-panel {
  width: 500px;
}
</style>
