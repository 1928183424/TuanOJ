package com.tzj.tuanojcodesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeRequest {
    /**
     * 输入用例
     */
    private List<String> inputList;
    /**
     * 代码
     */
    private String code;
    /**
     * 编程语言
     */
    private String language;
    /**
     * 判题配置信息
     */
    private JudgeConfig judgeConfig;
}
