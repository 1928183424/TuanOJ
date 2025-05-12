package com.tuan.tuanoj.judge.strategy;

import com.tuan.tuanoj.judge.codesandbox.model.JudgeInfo;
import com.tuan.tuanoj.model.dto.question.JudgeCase;
import com.tuan.tuanoj.model.dto.question.JudgeConfig;
import com.tuan.tuanoj.model.entity.Question;
import com.tuan.tuanoj.model.enums.JudgeInfoMessageEnum;

import java.util.List;
import java.util.Objects;

/**
 * Cpp 程序的判题策略
 */
public class CppLanguageJudgeStrategy implements JudgeStrategy {
    /**
     * 执行判题
     * @param judgeContext
     * @return
     */
    @Override
    public JudgeInfo doJudge(JudgeContext judgeContext) {
        JudgeInfo judgeInfo = judgeContext.getJudgeInfo();
        if(Objects.equals(judgeInfo.getMessage(), "Compile Error")){
            return judgeInfo;
        }
        List<String> inputList = judgeContext.getInputList();
        List<String> outputList = judgeContext.getOutputList();
        Question question = judgeContext.getQuestion();
        List<JudgeCase> judgeCaseList = judgeContext.getJudgeCaseList();
        JudgeConfig judgeConfig = judgeContext.getJudgeConfig();

        Long time = judgeInfo.getTime();
        Long memory = judgeInfo.getMemory();
        JudgeInfo judgeInfoResponse = new JudgeInfo();
        judgeInfoResponse.setMemory(memory);
        judgeInfoResponse.setTime(time);

        JudgeInfoMessageEnum judgeInfoMessageEnum = JudgeInfoMessageEnum.ACCEPTED;

        // 1）判断题目的限制是否符合要求
        Long timeLimit = judgeConfig.getTimeLimit();
        Long memoryLimit = judgeConfig.getMemoryLimit();
        if (time > timeLimit) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }
        if (memory > memoryLimit){
            judgeInfoMessageEnum = JudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }

        // 2）判断沙箱执行的结果输出数量是否与预期输出数量相等
        if(outputList.size() != inputList.size()){
            judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }
        // 3）依次判断每一项输出是否与预期输出相等
        for (int i = 0; i < judgeCaseList.size(); i++) {
            JudgeCase judgeCase = judgeCaseList.get(i);
            // 判断题目返回值是否是浮点数
            if(judgeConfig.getReturnType() == "double" || judgeConfig.getReturnType() == "float"){
                if(Math.abs(Double.parseDouble(judgeCase.getOutput()) - Double.parseDouble(outputList.get(i))) > 1e-5){
                    judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
                    judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue() + " 输入:" + judgeCase.getInput() + " 预计输出:" + judgeCase.getOutput() + " 实际输出:" + outputList.get(i));
                    return judgeInfoResponse;
                }
            }else{
                if (!judgeCase.getOutput().equals(outputList.get(i))){
                    judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
                    judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue() + " 输入:" + judgeCase.getInput() + " 预计输出:" + judgeCase.getOutput() + " 实际输出:" + outputList.get(i));
                    return judgeInfoResponse;
                }
            }
        }

        judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
        return judgeInfoResponse;
    }
}
