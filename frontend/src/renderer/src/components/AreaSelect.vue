<template>
  <div>
    <el-cascader
      ref="areaSelectRef"
      v-model="area"
      :options="AreaData"
      placeholder="请选择地区"
      clearable
      @change="change"
    ></el-cascader>
  </div>
</template>

<script setup>
import AreaData from "@/utils/AreaData";
import { computed, ref } from "vue";

const props = defineProps({
    modelValue: {
        type: Object,
        default: () => ({
            areaCode: [],
            areaName: [],
        }),
    },
});

const emit = defineEmits(["update:modelValue"]);
const areaSelectRef = ref();

const area = computed({
    get: () => props.modelValue?.areaCode || [],
    set: (value) => {
        emit("update:modelValue", {
            areaCode: value || [],
            areaName: props.modelValue?.areaName || [],
        });
    },
});


const change = () => {
    const checkedNode = areaSelectRef.value?.getCheckedNodes()?.[0];
    if (!checkedNode) {
        emit("update:modelValue", {
            areaCode: [],
            areaName: [],
        });
        return;
    }

    emit("update:modelValue", {
        areaCode: checkedNode.pathValues || [],
        areaName: checkedNode.pathLabels || [],
    });
};
</script>

<style lang="scss" scoped>
</style>
