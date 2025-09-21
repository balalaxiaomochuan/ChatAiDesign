package com.huawei.chataidesign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
public class ChatAiDesignApplication {

	public static void main(String[] args) {
		log.info("启动成功");
		SpringApplication.run(ChatAiDesignApplication.class, args);
	}

}
