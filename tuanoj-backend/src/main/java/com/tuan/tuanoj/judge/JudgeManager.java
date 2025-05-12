package com.tuan.tuanoj.judge;

import com.tuan.tuanoj.judge.codesandbox.model.JudgeInfo;
import com.tuan.tuanoj.judge.strategy.*;
import com.tuan.tuanoj.model.entity.QuestionSubmit;
import org.springframework.stereotype.Service;

/**
 * 判题管理（简化调用）
 */
@Service
public class JudgeManager {

    /**
     * 执行判题
     * @param judgeContext
     * @return
     */
    JudgeInfo doJudge(JudgeContext judgeContext){
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String language = questionSubmit.getLanguage();
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        if(language.equals("java")) {
            judgeStrategy = new JavaLanguageJudgeStrategy();
        }else if(language.equals("cpp")){
            judgeStrategy = new CppLanguageJudgeStrategy();
        }else if(language.equals("python")){
            judgeStrategy = new PythonLanguageJudgeStrategy();
        }
        return judgeStrategy.doJudge(judgeContext);
    }
}
