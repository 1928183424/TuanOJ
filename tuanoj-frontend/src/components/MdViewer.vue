<template>
  <Viewer :value="value" :plugins="plugins" />
</template>

<script setup lang="ts">
import gfm from "@bytemd/plugin-gfm";
import highlight from "@bytemd/plugin-highlight";
import { Viewer } from "@bytemd/vue-next";

/**
 * 定义组件属性类型
 */
interface Props {
  value: string;
}

/**
 * 给组件指定初始值
 */
// eslint-disable-next-line no-undef
const props = withDefaults(defineProps<Props>(), {
  value: () => "",
});

const plugins = [
  gfm(),
  highlight(),
  // Add more plugins here
];
</script>

<style></style>
