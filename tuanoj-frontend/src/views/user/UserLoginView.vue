<template>
  <div id="userLoginView">
    <h1>用户登录</h1>
    <a-form :model="form" :style="{ width: '550px' }" @submit="handleSubmit">
      <a-form-item field="userAccount" label="账号">
        <a-input v-model="form.userAccount" placeholder="请输入账号" />
      </a-form-item>
      <a-form-item field="userPassword" tooltip="密码不少于8位" label="密码">
        <a-input-password
          v-model="form.userPassword"
          placeholder="请输入密码"
        />
      </a-form-item>
      <a-form-item class="submit-btn">
        <a-button type="primary" html-type="submit">登录</a-button>
        <a-button
          style="margin-left: 10px"
          @click="route.push('/user/register')"
        >
          注册
        </a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script setup lang="ts">
import { reactive } from "vue";
import { UserControllerService, UserLoginRequest } from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import { useStore } from "vuex";
import { setManuallyLoggedIn } from "@/access/loginState";

const route = useRouter();
const store = useStore();
/**
 * 表单信息
 */
const form = reactive({
  userAccount: "",
  userPassword: "",
} as UserLoginRequest);
/**
 * 提交表单
 */
const handleSubmit = async () => {
  try {
    const res = await UserControllerService.userLoginUsingPost(form);

    if (res.code === 0) {
      console.log("登录响应:", res);

      // 保存登录响应中的用户信息到store
      store.commit("user/updateUser", res.data);

      // 设置手动登录标志，防止路由守卫中的getLoginUser覆盖
      setManuallyLoggedIn(true);

      // 不要调用getLoginUser，直接使用登录响应的用户数据
      // await store.dispatch("user/getLoginUser");

      message.success("登录成功");
      route.push("/");
    } else {
      message.error("登录失败," + res.message);
    }
  } catch (error) {
    console.error("登录请求异常:", error);
    message.error("登录异常，请稍后再试");
  }
};
</script>

<style scoped>
#userLoginView {
  position: absolute;
  top: 30%; /* 从页面顶部的 30% 处开始 */
  left: 50%;
  transform: translate(-50%, -30%); /* 水平居中，垂直调整 */
  width: 100%;
  max-width: 600px; /* 限制最大宽度 */
  padding: 20px;
}

h1 {
  text-align: center;
  margin-bottom: 20px;
}
</style>
