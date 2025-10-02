package com.huawei.chataidesign.entity.response;

import lombok.Data;
import java.io.Serializable;

/**
 * 通用响应类
 * @param <T> 响应数据类型
 */
@Data
public class CommonResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    public CommonResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    public CommonResponse(Integer code, String message) {
        this();
        this.code = code;
        this.message = message;
    }

    public CommonResponse(Integer code, String message, T data) {
        this(code, message);
        this.data = data;
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> CommonResponse<T> success() {
        return new CommonResponse<>(200, "Success");
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>(200, "Success", data);
    }

    /**
     * 成功响应（自定义消息和数据）
     */
    public static <T> CommonResponse<T> success(String message, T data) {
        return new CommonResponse<>(200, message, data);
    }

    /**
     * 失败响应
     */
    public static <T> CommonResponse<T> error(Integer code, String message) {
        return new CommonResponse<>(code, message);
    }

    /**
     * 失败响应（默认错误码500）
     */
    public static <T> CommonResponse<T> error(String message) {
        return new CommonResponse<>(500, message);
    }
}
