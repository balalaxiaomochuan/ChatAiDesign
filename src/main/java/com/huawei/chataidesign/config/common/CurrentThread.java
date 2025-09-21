package com.huawei.chataidesign.config.common;

import org.springframework.http.HttpHeaders;

/**
 * 当前线程上下文信息管理类
 * 使用ThreadLocal<HttpHeaders>为每个线程维护独立的HTTP头信息
 */
public class CurrentThread {
    // 使用HttpHeaders存储用户信息
    private static final ThreadLocal<HttpHeaders> HEADERS_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前线程的HTTP头信息
     * @param headers HttpHeaders实例
     */
    public static void setHeaders(HttpHeaders headers) {
        HEADERS_HOLDER.set(headers);
    }

    /**
     * 获取当前线程的HTTP头信息
     * @return HttpHeaders实例
     */
    public static HttpHeaders getHeaders() {
        return HEADERS_HOLDER.get();
    }

    /**
     * 获取当前线程的用户名
     * @return 用户名
     */
    public static String getUserName() {
        HttpHeaders headers = HEADERS_HOLDER.get();
        if (headers != null) {
            String userName = headers.getFirst("user-name");
            return userName != null ? userName : "Unknown";
        }
        return "Unknown";
    }

    /**
     * 获取当前线程的部门
     * @return 部门名称
     */
    public static String getDepart() {
        HttpHeaders headers = HEADERS_HOLDER.get();
        if (headers != null) {
            String depart = headers.getFirst("depart");
            return depart != null ? depart : "Unknown";
        }
        return "Unknown";
    }

    /**
     * 获取当前线程的用户ID
     * @return 用户ID
     */
    public static Long getUserId() {
        HttpHeaders headers = HEADERS_HOLDER.get();
        if (headers != null) {
            String userId = headers.getFirst("user-id");
            try {
                return userId != null ? Long.parseLong(userId) : -1L;
            } catch (NumberFormatException e) {
                return -1L;
            }
        }
        return -1L;
    }

    /**
     * 获取当前线程的用户邮箱
     * @return 用户邮箱
     */
    public static String getEmail() {
        HttpHeaders headers = HEADERS_HOLDER.get();
        if (headers != null) {
            String email = headers.getFirst("email");
            return email != null ? email : "Unknown";
        }
        return "Unknown";
    }

    /**
     * 获取当前线程的角色
     * @return 用户角色
     */
    public static String getRole() {
        HttpHeaders headers = HEADERS_HOLDER.get();
        if (headers != null) {
            String role = headers.getFirst("role");
            return role != null ? role : "Unknown";
        }
        return "Unknown";
    }

    /**
     * 获取指定header字段的值
     * @param headerName header名称
     * @return header值
     */
    public static String getHeader(String headerName) {
        HttpHeaders headers = HEADERS_HOLDER.get();
        if (headers != null) {
            String value = headers.getFirst(headerName);
            return value != null ? value : "Unknown";
        }
        return "Unknown";
    }

    /**
     * 清理当前线程的HTTP头信息
     * 防止内存泄漏，必须在请求结束时调用
     */
    public static void clear() {
        HEADERS_HOLDER.remove();
    }
}
