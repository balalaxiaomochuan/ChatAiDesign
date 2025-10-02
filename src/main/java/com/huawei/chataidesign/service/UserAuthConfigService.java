package com.huawei.chataidesign.service;

import com.huawei.chataidesign.ChatAiDesignApplication;
import com.huawei.chataidesign.config.generator.ImageGenerator;
import com.huawei.chataidesign.entity.User;
import com.huawei.chataidesign.exception.ChatAiDesignException;
import com.huawei.chataidesign.mapper.UserMapper;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class UserAuthConfigService {
    @Resource
    private UserMapper userMapper;
    public void register(@NotNull User userRegisterReq) throws IOException {
        log.info("User register request received.");
        if (!isValidPassword(userRegisterReq.getPassword())) {
            log.error("Invalid password : {}", userRegisterReq.getPassword());
            throw new ChatAiDesignException("Invalid password.");
        }
        // 检查当前用户名是否重复，按用户名检索数据库
        User user = userMapper.getUserByUserName(userRegisterReq.getUsername());
        if (user != null) {
            log.error("User already exists.");
            throw new ChatAiDesignException("User already exists.");
        }
        if (StringUtils.isEmpty(userRegisterReq.getEmail())) {
            throw new ChatAiDesignException("Email cannot be empty.");
        }
        if (StringUtils.isEmpty(userRegisterReq.getNickname())) {
            userRegisterReq.setNickname(userRegisterReq.getUsername());
        }
        if (StringUtils.isEmpty(userRegisterReq.getAvatar())) {
            userRegisterReq.setAvatar(ImageGenerator.generateLetterImageBase64(userRegisterReq.getUsername().charAt(0)));
        }
        userRegisterReq.setStatus(1);
        userRegisterReq.setUpdateAndCreateNowTime();
        userMapper.insert(userRegisterReq);
    }


    private boolean isValidPassword(String passwd) {
        // 密码不能为空
        if (StringUtils.isEmpty(passwd)) {
            return false;
        }
        // 密码长度至少8位
        if (passwd.length() < 8) {
            return false;
        }
        // 密码不能超过32位
        if (passwd.length() > 32) {
            return false;
        }
        // 必须包含至少一个数字
        if (!passwd.matches(".*\\d.*")) {
            return false;
        }
        // 必须包含至少一个字母
        if (!passwd.matches(".*[a-zA-Z].*")) {
            return false;
        }
        // 必须包含至少一个特殊字符
        if (!passwd.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            return false;
        }
        return true;
    }

}
