package com.huawei.chataidesign.controller;

import com.huawei.chataidesign.config.AppConfiguration;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HealthController {

    @Resource
    private DiscoveryClient discoveryClient;

    @Autowired
    private AppConfiguration appConfiguration;

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    /**
     * 获取当前服务信息
     */
    @GetMapping("/service/info")
    public String serviceInfo() {
        return "Service Name: " + appConfiguration.getName() +
                ", Enabled: " + appConfiguration.isEnabled() +
                ", Timeout: " + appConfiguration.getTimeout();
    }

    /**
     * 获取注册到 Nacos 的所有服务
     */
    @GetMapping("/services")
    public List<String> getServices() {
        return discoveryClient.getServices();
    }
}

