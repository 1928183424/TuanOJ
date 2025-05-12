package com.tuan.tuanoj.judge.strategy;

import com.tuan.tuanoj.judge.codesandbox.model.JudgeInfo;

public interface JudgeStrategy {
    /**
     * 执行判题
     * @param judgeContext
     * @return
     */
    JudgeInfo doJudge(JudgeContext judgeContext);
}
