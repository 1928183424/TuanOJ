<template>
  <div class="addQuestionView" style="margin-bottom: 64px">
    <h1 v-if="route.path.startsWith('/update')">更新题目</h1>
    <h1 v-else>创建题目</h1>
    <a-form :model="form">
      <a-form-item field="title" label="标题">
        <a-input
          :style="{ width: '650px' }"
          v-model="form.title"
          placeholder="请输入标题"
        />
      </a-form-item>
      <a-form-item field="tags" label="标签" style="text-align: left">
        <a-input-tag
          :style="{ width: '650px' }"
          v-model="form.tags"
          placeholder="请输入标签"
          allow-clear
        />
      </a-form-item>
      <a-form-item field="content" label="题目内容">
        <MdEditor :value="form.content" :handle-change="onContentChange" />
      </a-form-item>
      <a-form-item field="answer" label="题目答案">
        <MdEditor :value="form.answer" :handle-change="onAnswerChange" />
      </a-form-item>
      <a-form-item label="判题配置">
        <a-space direction="vertical" style="min-width: 480px">
          <a-form-item field="JudgeConfig.timeLimit" label="时间限制">
            <a-input-number
              v-model="form.judgeConfig.timeLimit"
              placeholder="请输入时间限制"
              mode="button"
              size="large"
              :min="0"
            />
          </a-form-item>
          <a-form-item field="JudgeConfig.memoryLimit" label="内存限制">
            <a-input-number
              v-model="form.judgeConfig.memoryLimit"
              placeholder="请输入内存限制"
              mode="button"
              size="large"
              :min="0"
            />
          </a-form-item>
          <a-form-item field="JudgeConfig.stackLimit" label="堆栈限制">
            <a-input-number
              v-model="form.judgeConfig.stackLimit"
              placeholder="请输入堆栈限制"
              mode="button"
              size="large"
              :min="0"
            />
          </a-form-item>
          <a-form-item field="JudgeConfig.methodName" label="方法名称">
            <a-input
              v-model="form.judgeConfig.methodName"
              placeholder="请输入方法名称"
              size="large"
              :min="0"
            />
          </a-form-item>
          <a-form-item field="JudgeConfig.paramTypes" label="输入参数类型">
            <a-input-tag
              v-model="form.judgeConfig.paramTypes"
              placeholder="请输入输入参数类型"
              :style="{ width: '100%' }"
              size="large"
              allow-clear
            />
          </a-form-item>
          <a-form-item field="JudgeConfig.returnType" label="返回参数类型">
            <a-input
              v-model="form.judgeConfig.returnType"
              placeholder="请输入返回参数类型"
              size="large"
              :min="0"
            />
          </a-form-item>
        </a-space>
      </a-form-item>
      <a-form-item
        label="判题用例配置"
        :content-flex="false"
        :merge-props="false"
      >
        <a-form-item
          v-for="(judgeCaseItem, index) of form.judgeCase"
          :key="index"
          no-style
        >
          <a-space direction="vertical" style="min-width: 480px">
            <a-form-item
              :field="`form.JudgeCase[${index}].input`"
              :label="`输入用例-${index}`"
              :key="index"
            >
              <a-input
                v-model="judgeCaseItem.input"
                placeholder="请输入判题输入用例"
              />
            </a-form-item>
            <a-form-item
              :field="`form.JudgeCase[${index}].output`"
              :label="`输出用例-${index}`"
              :key="index"
            >
              <a-input
                v-model="judgeCaseItem.output"
                placeholder="请输入判题输出用例"
              />
            </a-form-item>
            <a-button
              @click="handleDelete(index)"
              :style="{ marginBottom: '32px' }"
              status="danger"
            >
              删除</a-button
            >
          </a-space>
        </a-form-item>
      </a-form-item>
      <a-form-item>
        <a-button
          @click="handleAdd"
          type="outline"
          status="success"
          style="margin-bottom: 32px"
          >新增判题用例</a-button
        >
      </a-form-item>
      <a-form-item>
        <a-button
          v-if="route.path.startsWith('/update')"
          type="primary"
          style="min-width: 120px"
          @click="doSubmit"
          >更新</a-button
        >
        <a-button
          v-else
          type="primary"
          style="min-width: 120px"
          @click="doSubmit"
          >提交</a-button
        >
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import MdEditor from "@/components/MdEditor.vue";
import { QuestionControllerService } from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRoute, useRouter } from "vue-router";

const router = useRouter();
const route = useRoute();
//如果页面地址包含update，视为更新页面
const updatePage = route.path.includes("update");

// 初始值
const form = ref({
  answer: "暴力破解",
  content: "题目内容",
  judgeCase: [
    {
      input: "1 2",
      output: "3 4",
    },
  ],
  judgeConfig: {
    memoryLimit: 1000,
    stackLimit: 1000,
    timeLimit: 1000,
    methodName: "",
    paramTypes: "",
    returnType: "",
  },
  tags: ["栈", "简单"],
  title: "A + B",
});

/**
 * 根据题目id获取老的数据
 */
const loadData = async () => {
  //判断id是否存在
  const id = route.query.id;
  if (!id) {
    return;
  }
  const res = await QuestionControllerService.getQuestionByIdUsingGet(
    id as any
  );
  if (res.code === 0) {
    form.value = res.data as any;
    //如果判题用例为空，则给初始值
    if (!form.value.judgeCase) {
      form.value.judgeCase = [
        {
          input: "",
          output: "",
        },
      ];
    } else {
      //json对象转js对象
      form.value.judgeCase = JSON.parse(form.value.judgeCase as any);
    }
    //如果判题配置为空，则给初始值
    if (!form.value.judgeConfig) {
      form.value.judgeConfig = {
        memoryLimit: 1000,
        stackLimit: 1000,
        timeLimit: 1000,
        methodName: "",
        paramTypes: "[]",
        returnType: "",
      };
    } else {
      //json对象转js对象
      form.value.judgeConfig = JSON.parse(form.value.judgeConfig as any);
    }
    //如果标签为空，则给初始值
    if (!form.value.tags) {
      form.value.tags = [];
    } else {
      //json对象转js对象
      form.value.tags = JSON.parse(form.value.tags as any);
    }
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
 * 新增判题用例
 */
const handleAdd = () => {
  form.value.judgeCase.push({
    input: "",
    output: "",
  });
};

/**
 * 删除判题用例
 */
const handleDelete = (index: number) => {
  form.value.judgeCase.splice(index, 1);
};

const doSubmit = async () => {
  console.log(form.value);
  //区分更新还是创建
  if (updatePage) {
    const res = await QuestionControllerService.updateQuestionUsingPost(
      form.value
    );
    if (res.code === 0) {
      message.success("更新成功");
    } else {
      message.error("更新失败" + res.message);
    }
  } else {
    const res = await QuestionControllerService.addQuestionUsingPost(
      form.value
    );
    console.log(res);
    if (res.code === 0) {
      message.success("创建成功");
      /**
       * 跳转到主页
       */
      router.push({
        path: `/`,
      });
    } else {
      message.error("创建失败" + res.message);
    }
  }
};

const onContentChange = (value: string) => {
  form.value.content = value;
};

const onAnswerChange = (value: string) => {
  form.value.answer = value;
};
</script>

<style scoped>
.addQuestionView {
  max-width: 1280px;
  margin: 0 auto;
}
h1 {
  text-align: center;
}
</style>
