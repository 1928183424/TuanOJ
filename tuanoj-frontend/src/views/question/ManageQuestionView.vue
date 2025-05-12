<template>
  <div class="manageQuestionView" style="margin-bottom: 64px">
    <h1 style="text-align: center">管理题目</h1>
    <a-table
      style="margin: 30px 0"
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
      <template #optional="{ record }">
        <a-space>
          <a-button type="primary" @click="doUpdate(record)">修改</a-button>
          <a-button status="danger" @click="doDelete(record)">删除</a-button>
        </a-space>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watchEffect } from "vue";
import { Question, QuestionControllerService } from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
const show = ref(true);

const dataList = ref([]);
const total = ref(0);
const searchParams = ref({
  current: 1,
  pageSize: 10,
});

const loadData = async () => {
  const res = await QuestionControllerService.listQuestionByPageUsingPost(
    searchParams.value
  );
  if (res.code === 0) {
    dataList.value = res.data.records;
    total.value = res.data.total;
  } else {
    message.error("加载失败" + res.message);
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
    title: "id",
    dataIndex: "id",
    ellipsis: true,
    tooltip: true,
  },
  {
    title: "标题",
    dataIndex: "title",
    ellipsis: true,
    tooltip: true,
  },
  {
    title: "内容",
    dataIndex: "content",
    ellipsis: true,
    tooltip: true,
  },
  {
    title: "标签",
    dataIndex: "tags",
    ellipsis: true,
    tooltip: true,
  },
  {
    title: "答案",
    dataIndex: "answer",
    ellipsis: true,
    tooltip: true,
  },
  {
    title: "提交数",
    dataIndex: "submitNum",
    ellipsis: true,
    tooltip: true,
  },
  {
    title: "通过数",
    dataIndex: "acceptedNum",
    ellipsis: true,
    tooltip: true,
  },
  {
    title: "判题配置",
    dataIndex: "judgeConfig",
    ellipsis: true,
    tooltip: true,
  },
  {
    title: "判题用例",
    dataIndex: "judgeCase",
    ellipsis: true,
    tooltip: true,
  },
  {
    title: "用户id",
    dataIndex: "userId",
    ellipsis: true,
    tooltip: true,
  },
  {
    title: "创建时间",
    dataIndex: "createTime",
    ellipsis: true,
    tooltip: true,
  },
  {
    title: "操作",
    slotName: "optional",
    ellipsis: true,
    width: 180,
  },
];

/**
 * 删除题目
 * @param question
 */
const doDelete = async (question: Question) => {
  const res = await QuestionControllerService.deleteQuestionUsingPost({
    id: question.id,
  });
  if (res.code === 0) {
    message.success("删除成功");
    //更新数据
    loadData();
  } else {
    message.error("删除失败" + res.message);
  }
};

const router = useRouter();

/**
 * 更新题目
 * @param question
 */
const doUpdate = (question: Question) => {
  router.push({
    path: "/update/question",
    query: {
      id: question.id,
    },
  });
};

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
</script>
<style scoped>
.manageQuestionView {
  max-width: 1280px;
  margin: 0 auto;
}
</style>
