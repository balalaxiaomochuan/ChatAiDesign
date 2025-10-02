package com.huawei.chataidesign.entity.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LoginReq {
    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;
}
