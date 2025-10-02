package com.huawei.chataidesign.config.interceptor;

import com.huawei.chataidesign.config.SessionManager;
import com.huawei.chataidesign.entity.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Autowired
    private SessionManager sessionManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取Cookie中的sessionId
        Cookie[] cookies = request.getCookies();
        String sessionId = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("SESSION_ID".equals(cookie.getName())) {
                    sessionId = cookie.getValue();
                    break;
                }
            }
        }

        // 验证会话
        User user = sessionManager.getUserBySessionId(sessionId);
        if (user == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("未授权访问");
            return false;
        }

        // 将用户信息存储到request中供后续使用
        request.setAttribute("currentUser", user);
        return true;
    }
}
