package com.huawei.chataidesign.config.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class RequestLoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String remoteAddr = request.getRemoteAddr();

        String requestUrl = queryString != null ? uri + "?" + queryString : uri;

        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);

        log.info(">>> API Request: {} {} from {}", method, requestUrl, remoteAddr);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) {
        // 可以在这里添加响应前的处理
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String requestUrl = queryString != null ? uri + "?" + queryString : uri;

        long startTime = (Long) request.getAttribute("startTime");
        long duration = System.currentTimeMillis() - startTime;
        int status = response.getStatus();

        log.info("<<< API Response: {} {} - Status: {} - Duration: {}ms",
                method, requestUrl, status, duration);

        if (ex != null) {
            log.error("Exception occurred: ", ex);
        }
    }
}
