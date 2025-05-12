<template>
  <a-row id="globalHeader" align="center" style="margin-bottom: 16px">
    <a-col flex="auto">
      <a-menu
        mode="horizontal"
        :selectedKeys="selectedKeys"
        @menu-item-click="doMenuClick"
      >
        <a-menu-item
          key="0"
          :style="{ padding: 0, marginRight: '38px' }"
          disabled
        >
          <!-- 给导航栏添加logo和标题 -->
          <div class="title-bar">
            <img class="logo" src="../assets/sunflower.svg" />
            <div class="title">团OJ</div>
          </div>
        </a-menu-item>
        <!-- 动态绑定：通过for循环来遍历visibleRoutes中的项目添加到导航栏中 -->
        <a-menu-item v-for="item in visibleRoutes" :key="item.path"
          >{{ item.name }}
        </a-menu-item>
      </a-menu>
    </a-col>
    <!-- 显示用户名 -->
    <!-- 让里面的元素水平排列 -->
    <a-col flex="50px">
      <div>{{ store.state.user?.loginUser?.userName ?? "未登录" }}</div>
    </a-col>
    <a-col flex="100px">
      <div>
        <a-button type="primary" @click="doMenuClick('/user/login')">
          {{ "重新登录" }}
        </a-button>
      </div>
    </a-col>
  </a-row>
</template>

<script setup lang="ts">
import { routes } from "@/router/routes";
import { useRoute, useRouter } from "vue-router";
import { computed, ref } from "vue";
import { useStore } from "vuex";
import checkAccess from "@/access/checkAccess";

const router = useRouter();
const route = useRoute();
const store = useStore();

//默认选择主页
const selectedKeys = ref([route.path]);

//菜单只展示在visibleRoutes中的页面
//注意，这里使用计算属性computed，是为了当登陆用户发生变化的时候，触发菜单栏的重新渲染，展示新增权限的菜单项
const visibleRoutes = computed(() => {
  return routes.filter((item, index) => {
    //如果页面是隐藏的，则不展示
    if (item.meta?.hideInMenu) {
      return false;
    }
    //如果页面权限和用户权限不匹配，则不展示
    if (
      !checkAccess(store.state.user?.loginUser, item?.meta?.access as string)
    ) {
      return false;
    }
    return true;
  });
});

//编写路由跳转函数（点击导航栏的文字进行跳转），用@menu-item-click="doMenuClick"绑定点击事件
const doMenuClick = (key: string) => {
  router.push({ path: key });
};

//路由跳转后，更新选中的菜单项，这样可以同步导航栏的高亮状态
router.afterEach((to, from, failure) => {
  selectedKeys.value = [to.path];
});
</script>

<style scoped>
.title-bar {
  display: flex;
  align-items: center;
}

.logo {
  height: 48px;
}

.title {
  color: #444;
  margin-left: 10px;
}
</style>
