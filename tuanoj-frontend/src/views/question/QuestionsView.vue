<template>
  <div class="questionsView" style="margin-bottom: 64px">
    <a-form :model="searchParams" layout="inline">
      <a-form-item field="title" label="名称" style="min-width: 240px">
        <a-input v-model="searchParams.title" placeholder="请输入名称" />
      </a-form-item>
      <a-form-item field="tags" label="标签" style="min-width: 240px">
        <a-input-tag v-model="searchParams.tags" placeholder="请输入标签" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="doSubmit">提交</a-button>
      </a-form-item>
    </a-form>
    <a-divider size="0" />
    <a-table
      :columns="columns"
      :data="dataList"
      :pagination="{
        showTotal: true,
        pageSize: searchParams.pageSize,
        current: searchParams.current,
        total,
      }"
      @page-change="onPageChange"
    >
      <template #tags="{ record }">
        <a-space wrap>
          <a-tag
            v-for="(tag, index) of record.tags"
            :key="index"
            color="green"
            >{{ tag }}</a-tag
          >
        </a-space>
      </template>
      <template #acceptedRate="{ record }">
        {{
          record.submitNum > 0
            ? `${((record.acceptedNum / record.submitNum) * 100).toFixed(
                1
              )}% (${record.acceptedNum} / ${record.submitNum})`
            : "0% (0 / 0)"
        }}
      </template>
      <template #createTime="{ record }">
        {{ moment(record.createTime).format("YYYY-MM-DD") }}</template
      >
      <template #optional="{ record }">
        <a-space
          ><a-button type="primary" @click="toQuestionPage(record)"
            >做题</a-button
          >
        </a-space>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watchEffect } from "vue";
import {
  Question,
  QuestionControllerService,
  QuestionQueryRequest,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import moment from "moment";

const show = ref(true);

const dataList = ref([]);
const total = ref(0);
const searchParams = ref<QuestionQueryRequest>({
  current: 1,
  pageSize: 10,
  title: "",
  tags: [],
});

const loadData = async () => {
  try {
    const res = await QuestionControllerService.listQuestionVoByPageUsingPost(
      searchParams.value
    );
    console.log("API响应完整内容:", res);

    if (res.code === 0) {
      dataList.value = res.data.records;
      total.value = res.data.total;

      // 数据加载后检查一下字段情况，方便调试
      if (dataList.value.length > 0) {
        console.log("第一条数据示例:", dataList.value[0]);
      }
    } else {
      // 查看详细错误信息
      console.error("API错误详情:", res);

      // 如果是未登录错误，尝试重新登录
      if (res.code === 40100 || res.message.includes("未登录")) {
        message.error("登录已过期，请重新登录");
        // 可选：跳转到登录页
        router.push("/user/login");
      } else {
        message.error("加载失败" + res.message);
      }
    }
  } catch (error) {
    console.error("请求异常:", error);
    message.error("请求异常，请检查网络连接");
  }
};

/**
 * 页面加载时，请求数据
 */
onMounted(() => {
  loadData();
});

/**
 * 监听searchParams的值,当其变化时，重新加载数据
 */
watchEffect(() => {
  loadData();
});

//格式：{id: "1867922511371563009", title: "两数之和", content: "实现a + b", tags: "["easy"]", answer: "11",…}

const columns = [
  {
    title: "题号",
    dataIndex: "id",
  },
  {
    title: "题目名称",
    dataIndex: "title",
  },
  {
    title: "标签",
    slotName: "tags",
  },
  {
    title: "通过率",
    slotName: "acceptedRate",
  },
  {
    title: "创建时间",
    slotName: "createTime",
  },
  {
    slotName: "optional",
  },
];

const router = useRouter();

/**
 * 切换分页时，更新searchParams的值，并重新加载数据
 * @param page
 */
const onPageChange = (page: number) => {
  searchParams.value = {
    ...searchParams.value,
    current: page,
  };
};

/**
 * 跳转到题目详情页（做题页）
 * @param question
 */
const toQuestionPage = (question: Question) => {
  router.push({
    path: `/view/question/${question.id}`,
  });
};

/**
 * 进行搜索
 */
const doSubmit = () => {
  searchParams.value = {
    ...searchParams.value,
    //这里需要重置搜索页号
    current: 1,
  };
};
</script>
<style scoped>
.questionsView {
  max-width: 1280px;
  margin: 0 auto;
}
</style>
