package com.huawei.chataidesign.config.interceptor;

import com.huawei.chataidesign.config.common.CurrentThread;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.http.HttpHeaders;

/**
 * 用户信息拦截器
 * 从HTTP请求头中提取用户信息并存储到ThreadLocal<HttpHeaders>中
 */
@Component
public class UserInfoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // 创建HttpHeaders实例
        HttpHeaders headers = new HttpHeaders();

        // 从请求头中提取用户信息
        String userName = request.getHeader("user-name");
        String depart = request.getHeader("depart");
        String userId = request.getHeader("user-id");
        String email = request.getHeader("email");
        String role = request.getHeader("role");

        // 设置用户信息到HttpHeaders中，如果字段为空则使用"Unknown"
        headers.add("user-name", userName != null ? userName : "Unknown");
        headers.add("depart", depart != null ? depart : "Unknown");
        headers.add("user-id", userId != null ? userId : "-1");
        headers.add("email", email != null ? email : "Unknown");
        headers.add("role", role != null ? role : "Unknown");

        // 将HttpHeaders实例存储到当前线程的ThreadLocal中
        CurrentThread.setHeaders(headers);

        return true; // 继续执行后续拦截器和处理器
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception {
        // 请求处理完成后可以做一些处理，但用户信息还不能清理
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        // 请求完全处理完成后清理ThreadLocal，防止内存泄漏
        CurrentThread.clear();
    }
}


