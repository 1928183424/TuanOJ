package com.tuan.tuanoj.judge;

import cn.hutool.json.JSONUtil;
import com.tuan.tuanoj.common.ErrorCode;
import com.tuan.tuanoj.exception.BusinessException;
import com.tuan.tuanoj.judge.codesandbox.CodeSandbox;
import com.tuan.tuanoj.judge.codesandbox.CodeSandboxFactory;
import com.tuan.tuanoj.judge.codesandbox.CodeSandboxProxy;
import com.tuan.tuanoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.tuan.tuanoj.judge.codesandbox.model.ExecuteCodeResponse;
import com.tuan.tuanoj.judge.codesandbox.model.JudgeInfo;
import com.tuan.tuanoj.judge.strategy.JudgeContext;
import com.tuan.tuanoj.model.dto.question.JudgeCase;
import com.tuan.tuanoj.model.dto.question.JudgeConfig;
import com.tuan.tuanoj.model.entity.Question;
import com.tuan.tuanoj.model.entity.QuestionSubmit;
import com.tuan.tuanoj.model.enums.JudgeInfoMessageEnum;
import com.tuan.tuanoj.model.enums.QuestionSubmitStatusEnum;
import com.tuan.tuanoj.service.QuestionService;
import com.tuan.tuanoj.service.QuestionSubmitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class JudgeServiceImpl implements JudgeService {

    @Value("${codesandbox.type:example}")
    private String type;

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private JudgeManager judgeManager;

    @Override
    public QuestionSubmit doJudge(long questionSubmitId) {
        // 1. 传入题目的提交id，获取到对应的题目、提交信息（包含代码、编程语言等）
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交记录不存在");
        }
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        // 2. 如果题目提交状态不为等待中，就不用重复执行了
        if(!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WATTING.getValue())){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目正在判题中");
        }
        // 3. 更改判题（题目提交）的状态为“判题中”，防止重复执行，也能让用户即时看到状态
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean update = questionSubmitService.updateById(questionSubmitUpdate);
        if(!update){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }
        // 4. 调用沙箱，获取到执行结果
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        codeSandbox = new CodeSandboxProxy(codeSandbox);
        // 获取示例输入列表
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        // 获取题目提交代码
        String code = questionSubmit.getCode();
        // 获取题目提交语言
        String language = questionSubmit.getLanguage();
        // 执行代码沙箱
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder().code(code).language(language).inputList(inputList).judgeConfig(JSONUtil.toBean(question.getJudgeConfig(), JudgeConfig.class)).build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        List<String> outputList = executeCodeResponse.getOutputList();

        // 5. 根据沙箱的执行结果，设置题目的判题状态
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
        judgeContext.setJudgeCaseList(judgeCaseList);
        judgeContext.setInputList(inputList);
        judgeContext.setOutputList(outputList);
        judgeContext.setQuestion(question);
        judgeContext.setQuestionSubmit(questionSubmit);
        judgeContext.setJudgeConfig(executeCodeRequest.getJudgeConfig());
        JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);

        // 6. 修改数据库中的判题结果
        questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCESS.getValue());
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        update = questionSubmitService.updateById(questionSubmitUpdate);
        if(!update){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }
        QuestionSubmit questionSubmitResult = questionSubmitService.getById(questionSubmitId);

        // 7.更新题目的提交数和通过数
        // 直接计算并设置，避免中间变量
        Question questionUpdate = new Question();
        questionUpdate.setId(questionId);
        questionUpdate.setSubmitNum((question.getSubmitNum() != null ? question.getSubmitNum() : 0) + 1);

        if (Objects.equals(judgeInfo.getMessage(), JudgeInfoMessageEnum.ACCEPTED.getValue())) {
            questionUpdate.setAcceptedNum((question.getAcceptedNum() != null ? question.getAcceptedNum() : 0) + 1);
        }
        boolean updateQuestion = questionService.updateById(questionUpdate);
        if(!updateQuestion){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目提交数更新错误");
        }

        return questionSubmitResult;
    }
}
