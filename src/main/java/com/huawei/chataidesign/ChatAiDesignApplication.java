package com.huawei.chataidesign;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.huawei.chataidesign.mapper")
public class ChatAiDesignApplication {
	public static void main(String[] args) {
		SpringApplication.run(ChatAiDesignApplication.class, args);
	}
}
