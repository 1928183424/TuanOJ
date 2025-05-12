package com.tuan.tuanoj.judge.codesandbox.impl;

import com.tuan.tuanoj.judge.codesandbox.CodeSandbox;
import com.tuan.tuanoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.tuan.tuanoj.judge.codesandbox.model.ExecuteCodeResponse;
import com.tuan.tuanoj.judge.codesandbox.model.JudgeInfo;
import com.tuan.tuanoj.model.enums.JudgeInfoMessageEnum;
import com.tuan.tuanoj.model.enums.QuestionSubmitStatusEnum;

import java.util.List;

/**
 * 示例代码沙箱（仅为了跑通任务流程）
 */
public class ExampleCodeSandbox implements CodeSandbox {

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(inputList);
        executeCodeResponse.setMessage("测试执行成功");
        executeCodeResponse.setStatus(QuestionSubmitStatusEnum.SUCCESS.getValue());
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage(JudgeInfoMessageEnum.WAITING.getValue());
        judgeInfo.setMemory(100L);
        judgeInfo.setTime(100L);
        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }
}
