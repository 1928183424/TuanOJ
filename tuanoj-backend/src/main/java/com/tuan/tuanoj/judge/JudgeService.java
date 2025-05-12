package com.tuan.tuanoj.judge;

import com.tuan.tuanoj.model.entity.QuestionSubmit;

public interface JudgeService {

    /**
     * 判题
     * @param questionSubmitId
     * @return
     */
    QuestionSubmit doJudge(long questionSubmitId);
}
