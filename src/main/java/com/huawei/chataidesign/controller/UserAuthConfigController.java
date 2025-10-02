package com.huawei.chataidesign.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.huawei.chataidesign.entity.User;
import com.huawei.chataidesign.entity.request.LoginReq;
import com.huawei.chataidesign.entity.response.CommonResponse;
import com.huawei.chataidesign.service.UserAuthConfigService;
import com.huawei.chataidesign.utils.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/auth")
@Slf4j
public class UserAuthConfigController {


    @Autowired
    private UserAuthConfigService userAuthConfigService;
    @PostMapping("/login")
    public ResponseEntity<JsonNode> login(@RequestBody LoginReq req) {
        log.info("User login request received.");
        ObjectNode result = JacksonUtil.createObjectNode();
        result.put("status", "success");
        result.put("messge", "login ok");
        return new ResponseEntity<>(result, HttpStatus.OK);
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
