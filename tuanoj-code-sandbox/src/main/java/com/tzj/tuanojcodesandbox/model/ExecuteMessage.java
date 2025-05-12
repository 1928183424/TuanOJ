package com.tzj.tuanojcodesandbox.model;

import lombok.Data;
import lombok.Setter;

/**
 * 进程执行信息
 */
@Data
public class ExecuteMessage {
    /**
     * 退出代码
     */
    private Integer exitValue;
    /**
     * 执行信息
     */
    private String message;
    /**
     * 错误信息
     */
    private String errorMessage;
    /**
     * 执行时间
     */
    private Long time;
    /**
     * 执行内存
     */
    @Setter
    private Long memory;
}
