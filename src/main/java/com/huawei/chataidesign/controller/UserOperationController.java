package com.huawei.chataidesign.controller;

import com.huawei.chataidesign.entity.User;
import com.huawei.chataidesign.entity.response.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/user")
public class UserOperationController {
    @PostMapping("/info/query  ")
    public CommonResponse<String> register(@RequestBody User userReq) {
        try {
            log.info("User register request received.");
//            userAuthConfigService.register(userReq);
            return CommonResponse.success(userReq.getNickname() + " register success!");
        } catch (Exception e) {
            log.error("Error during user registration: ", e);
            return CommonResponse.error(e.getMessage());
        }
    }
}
