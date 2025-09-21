package com.huawei.chataidesign.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@Data
@RefreshScope  // 支持配置自动刷新
@ConfigurationProperties(prefix = "app.config")
public class AppConfiguration {
    private String name = "default";
    private int timeout = 5000;
    private boolean enabled = true;
}

