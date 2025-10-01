package com.huawei.chataidesign.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/auth")
@Slf4j
public class UserAuthConfigController {

    @Autowired
    private UserAuthConfigService userAuthConfigService;
    @GetMapping("/login")
    public String login() {
        log.info("User login request received.");
        return "Login successful!";
    }

}
