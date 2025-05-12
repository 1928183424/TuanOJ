package com.tuan.tuanoj.judge.strategy;

import com.tuan.tuanoj.judge.codesandbox.model.JudgeInfo;
import com.tuan.tuanoj.model.dto.question.JudgeCase;
import com.tuan.tuanoj.model.dto.question.JudgeConfig;
import com.tuan.tuanoj.model.entity.Question;
import com.tuan.tuanoj.model.entity.QuestionSubmit;
import lombok.Data;

import java.util.List;

/**
 * 上下文（用于定义在策略中传递的参数）
 */
@Data
public class JudgeContext {
    private JudgeInfo judgeInfo;

    private List<JudgeCase> judgeCaseList;

    private List<String> inputList;

    private List<String> outputList;

    private Question question;

    private QuestionSubmit questionSubmit;

    private JudgeConfig judgeConfig;
}
