<template>
  <div class="viewQuestionViewNew" style="margin-bottom: 64px">
    <a-row :gutter="[24, 24]">
      <a-col :md="12" :xs="24">
        <a-tabs default-active-key="question">
          <a-tab-pane key="question" title="题目">
            <a-card v-if="question" :title="question.title">
              <a-descriptions
                title="判题条件"
                :column="{ xs: 1, md: 2, lg: 3 }"
              >
                <a-descriptions-item label="时间限制">
                  {{ question.judgeConfig.timeLimit ?? 0 }}
                </a-descriptions-item>
                <a-descriptions-item label="内存限制">
                  {{ question.judgeConfig.memoryLimit ?? 0 }}
                </a-descriptions-item>
                <a-descriptions-item label="堆栈限制">
                  {{ question.judgeConfig.stackLimit ?? 0 }}
                </a-descriptions-item>
              </a-descriptions>
              <MdViewer :value="question.content || ''" />
              <template #extra>
                <a-space wrap>
                  <a-tag
                    v-for="(tag, index) of question.tags"
                    :key="index"
                    color="green"
                    >{{ tag }}
                  </a-tag>
                </a-space>
              </template>
            </a-card>
          </a-tab-pane>
          <a-tab-pane key="comments" title="评论区" disabled>评论区</a-tab-pane>
          <a-tab-pane key="answer" title="答案"
            ><div v-if="question">
              <MdViewer :value="question.answer || ''" /></div
          ></a-tab-pane>
        </a-tabs>
      </a-col>
      <a-col :md="12" :xs="24">
        <a-form layout="inline">
          <a-form-item
            field="language"
            label="编程语言"
            style="min-width: 240px"
          >
            <a-select
              v-model="form.language"
              :style="{ width: '320px' }"
              placeholder="请选择编程语言"
              @change="handleLanguageChange"
            >
              <a-option>java</a-option>
              <a-option>cpp</a-option>
              <a-option>python</a-option>
            </a-select>
          </a-form-item>
        </a-form>
        <CodeEditor
          :value="form.code as string"
          :language="form.language"
          :handle-change="changeCode"
        />
        <a-divider size="0" />
        <a-button type="primary" style="min-width: 120px" @click="doSubmit">
          提交代码
        </a-button>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import {
  onMounted,
  ref,
  withDefaults,
  defineProps,
  watch,
  nextTick,
} from "vue";
import {
  QuestionControllerService,
  QuestionSubmitAddRequest,
  QuestionSubmitControllerService,
  QuestionVO,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import CodeEditor from "@/components/CodeEditor.vue";
import MdViewer from "@/components/MdViewer.vue";
import { useRouter } from "vue-router";

const question = ref<QuestionVO>();

const router = useRouter();

interface Props {
  id: string;
}

const props = withDefaults(defineProps<Props>(), { id: () => "" });

const loadData = async () => {
  const res = await QuestionControllerService.getQuestionVoByIdUsingGet(
    props.id as any
  );
  if (res.code === 0) {
    question.value = res.data;
    console.log("获取到的问题数据:", question.value);

    // 设置初始代码模板
    updateCodeTemplate(form.value.language);
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

// 监听问题数据变化，确保在数据加载后更新代码模板
watch(
  () => question.value,
  (newVal) => {
    if (newVal) {
      nextTick(() => {
        updateCodeTemplate(form.value.language);
      });
    }
  }
);

const form = ref<QuestionSubmitAddRequest>({
  code: "",
  language: "java",
});

/**
 * 提交代码
 */
const doSubmit = async () => {
  if (!question.value?.id) {
    return;
  }
  const res = await QuestionSubmitControllerService.doQuestionSubmitUsingPost({
    ...form.value,
    questionId: question.value.id,
  });
  if (res.code === 0) {
    message.success("提交成功");
    // 使用router进行页面跳转
    // 等待1秒后跳转
    setTimeout(() => {
      router.push("/questions_submit");
    }, 1000);
  } else {
    message.error("提交失败" + res.message);
  }
};

/**
 * 更新代码模板的公共函数
 */
const updateCodeTemplate = (language: string) => {
  if (!question.value?.judgeConfig?.codeTemplates) {
    console.log("没有找到codeTemplates属性");
    return;
  }

  console.log("原始codeTemplates:", question.value.judgeConfig.codeTemplates);

  try {
    // 检查codeTemplates是否已经是对象
    let codeTemplates;
    if (typeof question.value.judgeConfig.codeTemplates === "string") {
      codeTemplates = JSON.parse(question.value.judgeConfig.codeTemplates);
    } else {
      codeTemplates = question.value.judgeConfig.codeTemplates;
    }

    console.log("解析后的codeTemplates:", codeTemplates);
    console.log("当前选择的语言:", language);

    if (codeTemplates[language]) {
      console.log("找到对应语言的模板:", codeTemplates[language]);
      form.value.code = codeTemplates[language];
    } else {
      console.log("没有找到对应语言的模板，清空编辑器");
      form.value.code = "";
    }
  } catch (e) {
    console.error("解析代码模板失败", e);
    message.error("解析代码模板失败");
  }
};

/**
 * 当编程语言变化时，更新代码模板
 */
const handleLanguageChange = (value: string) => {
  updateCodeTemplate(value);
};

const changeCode = (value: string) => {
  form.value.code = value;
};
</script>
<style>
.viewQuestionViewNew {
  max-width: 1280px;
  margin: 0 auto;
}

.viewQuestionViewNew .arco-space-horizontal .arco-space-item {
  margin-bottom: 0 !important;
}
</style>
