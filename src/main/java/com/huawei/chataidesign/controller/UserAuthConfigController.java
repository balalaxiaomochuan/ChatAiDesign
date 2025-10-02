package com.huawei.chataidesign.controller;

import com.huawei.chataidesign.entity.User;
import com.huawei.chataidesign.entity.response.CommonResponse;
import com.huawei.chataidesign.service.UserAuthConfigService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/auth")
@Slf4j
public class UserAuthConfigController {
    @Autowired
    private UserAuthConfigService userAuthConfigService;

    @PostMapping("/login")
    public CommonResponse<String> login(@RequestBody User req, HttpServletResponse response) {
        try {
            log.info("User login request received.");
            userAuthConfigService.login(req, response);
            return CommonResponse.success(req.getNickname() + " login success!");
        } catch (Exception e) {
            log.error("Error during user login: ", e);
            return CommonResponse.error(e.getMessage());
        }
    }


    @PostMapping("/register")
    public CommonResponse<String> register(@RequestBody User userReq) {
        try {
            log.info("User register request received.");
            userAuthConfigService.register(userReq);
            return CommonResponse.success(userReq.getNickname() + " register success!");
        } catch (Exception e) {
            log.error("Error during user registration: ", e);
            return CommonResponse.error(e.getMessage());
        }
    }
}
