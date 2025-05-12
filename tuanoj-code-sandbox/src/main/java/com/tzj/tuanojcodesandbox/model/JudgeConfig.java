package com.tzj.tuanojcodesandbox.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class JudgeConfig {
    /**
     * 时间限制（ms）
     */
    private Long timeLimit;

    /**
     * 内存限制（KB）
     */
    private Long memoryLimit;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 参数类型列表
     */
    private List<String> paramTypes;

    /**
     * 返回值类型
     */
    private String returnType;

    /**
     * 代码模板
     */
    private Map<String, String> codeTemplates;
}
