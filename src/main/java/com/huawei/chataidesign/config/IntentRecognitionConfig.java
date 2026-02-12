package com.huawei.chataidesign.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 意图识别配置类
 * 用于管理意图识别相关的配置参数
 */
@Component
@Data
@ConfigurationProperties(prefix = "intent.recognition")
public class IntentRecognitionConfig {
    
    /**
     * 是否启用意图识别功能
     */
    private boolean enabled = true;
    
    /**
     * 缓存配置
     */
    private CacheConfig cache = new CacheConfig();
    
    /**
     * 默认置信度阈值
     */
    private double defaultConfidenceThreshold = 0.7;
    
    /**
     * 最大输入长度限制
     */
    private int maxInputLength = 1000;
    
    /**
     * 日志级别
     */
    private String logLevel = "INFO";
    
    /**
     * 缓存配置内部类
     */
    @Data
    public static class CacheConfig {
        /**
         * 是否启用缓存
         */
        private boolean enabled = true;
        
        /**
         * 缓存过期时间（分钟）
         */
        private int ttlMinutes = 30;
        
        /**
         * 缓存最大条目数
         */
        private int maxSize = 10000;
    }
    
    /**
     * 验证配置是否有效
     */
    public boolean isValid() {
        return defaultConfidenceThreshold >= 0.0 && defaultConfidenceThreshold <= 1.0 &&
               maxInputLength > 0 &&
               cache.ttlMinutes > 0;
    }
}