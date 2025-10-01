package com.huawei.chataidesign.exception;

import lombok.Data;

/**
 * ChatAiDesign 业务异常类
 * 用于处理业务逻辑中的异常情况
 */
@Data
public class ChatAiDesignException extends RuntimeException {

    private int errorCode;
    private String errorMessage;

    public ChatAiDesignException(String message) {
        super(message);
        this.errorMessage = message;
    }

    public ChatAiDesignException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errorMessage = message;
    }

    public ChatAiDesignException(String message, Throwable cause) {
        super(message, cause);
        this.errorMessage = message;
    }

    public ChatAiDesignException(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorMessage = message;
    }
}
