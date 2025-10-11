package com.huawei.chataidesign.config;

import com.huawei.chataidesign.config.interceptor.AuthenticationInterceptor;
import com.huawei.chataidesign.config.interceptor.RequestLoggingInterceptor;
import com.huawei.chataidesign.config.interceptor.UserInfoInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private RequestLoggingInterceptor requestLoggingInterceptor;

    @Resource
    private UserInfoInterceptor userInfoInterceptor;

    @Resource
    private AuthenticationInterceptor authenticationInterceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLoggingInterceptor)
                .addPathPatterns("/**")  // 拦截所有路径
                .excludePathPatterns("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**"); // 排除某些路径
        // 注册用户信息拦截器
        registry.addInterceptor(userInfoInterceptor)
                .addPathPatterns("/**")  // 拦截所有请求
                .excludePathPatterns("/health", "/metrics", "/favicon.ico"); // 排除健康检查等路径

//        registry.addInterceptor(authenticationInterceptor)
//                .addPathPatterns("/**")
//                .excludePathPatterns("/user/auth/register", "/user/auth/login");

    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*") // 明确指定允许的源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
