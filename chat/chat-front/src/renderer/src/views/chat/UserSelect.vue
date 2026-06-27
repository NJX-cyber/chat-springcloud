<template>
    <div>
        <Dialogue :show="dialogConfig.show" :title="dialogConfig.title" :buttons="dialogConfig.buttons" width="660px"
            @close="dialogConfig.show = false">
            <el-transfer v-model="formData.selectContacts" :titles="['全部', '已选']"
                :format="{ noChecked: '${total}', hasChecked: '${checked}/${total}' }" :data="dataList"
                :props="{ label: 'contactName', key: 'contactId' }" filterable filter-method="search()">
                <template #default="{ option }">
                    <div class="select-item">
                        <AvatarBase :userId="option.contactId" width="30" border-radius="5" :showDetail="false" />
                        <div class="nick-name">{{ option.contactName }}</div>
                    </div>
                </template>
            </el-transfer>
        </Dialogue>
    </div>
</template>

<script setup>
import AvatarBase from "@/components/AvatarBase.vue"
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();

const dialogConfig = reactive({
    show: false,
    title: "选择联系人",
    buttons: [
        {
            type: "danger",
            text: "确定",
            click: (e) => {
                submitForm();
            },
        },
    ],
});

const search = (query, item) => {
    return item.contactName.toLowerCase().includes(query.toLowerCase());
};

const dataList = ref([]);
const formData = ref({ selectContacts: [] })

const show = ({ contactList, groupId, opType }) => {
    formData.value.title = opType == 0 ? "移出群成员" : "添加群成员";
    dataList.value = contactList;
    dialogConfig.show = true;
    formData.value = {
        selectContacts: [],
        groupId: groupId,
        opType: opType
    }
}

const submitForm = async () => {
    if (formData.value.selectContacts.length == 0) {
        proxy.Message.warning("请选择联系人");
        return;
    }
    let params = {}
    Object.assign(params, formData.value);
    let result = await proxy.Request({
        url: proxy.Api.addOrRemoveGroupMember,
        method: "post",
        params,
    });
    if (!result) {
        return;
    }
    dialogConfig.show = false;
    emit("callback");
}

defineExpose({
    show
})

const emit = defineEmits(["callback"]);

</script>

<style lang="scss" scoped>
.el-transfer {
    width: 100%;
    display: block !important;
    display: flex;

    :deep(.el-transfer-panel) {
        width: 220px;
    }

    :deep(.el-transfer-panel__item) {
        display: flex;
        align-items: center;
        justify-content: center;
        margin-top: 5px;
    }
}

:deep(.el-transfer__button) {
    width: 60px;
    flex-direction: column;
    text-align: center;
    padding: 0 5px;

    .el-transfer__button {
        text-align: center;
        margin-left: 0;
        margin-right: 0;
        margin-top: 5px;
        padding: 5px;
        height: 36px;
        border-radius: 50%;
    }
}

:deep(.el-transfer__buttons) {
    padding: 0 5px;
}

.select-item {
    display: flex;

    .avatar {
        width: 30px;
        height: 30px;
    }

    .nick-name {
        flex: 1;
        margin-left: 5px;
    }

}
</style>
