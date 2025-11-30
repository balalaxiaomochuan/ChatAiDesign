package com.huawei.chataidesign.entity.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PromptReq {
    @JsonProperty("prompt")
    private String prompt;

    @JsonProperty("memory_id")
    private int memoryId;
}
