package com.huawei.chataidesign.entity.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 带意图识别的提示请求实体
 * 扩展原有的PromptReq，增加意图识别相关字段
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IntentPromptReq extends PromptReq {
    
    /**
     * 是否启用意图识别
     */
    @JsonProperty("enable_intent_recognition")
    private Boolean enableIntentRecognition = true;
    
    /**
     * 最小置信度阈值 (0.0 - 1.0)
     * 低于此阈值的意图将被视为不明确
     */
    @JsonProperty("min_confidence")
    private Double minConfidence = 0.7;
    
    /**
     * 是否需要确认意图
     */
    @JsonProperty("require_confirmation")
    private Boolean requireConfirmation = false;
    
    /**
     * 上下文信息，用于更好的意图理解
     */
    @JsonProperty("context")
    private String context;
    
    /**
     * 会话历史，用于上下文理解
     */
    @JsonProperty("conversation_history")
    private String conversationHistory;
    
    public IntentPromptReq() {
        super();
    }
    
    public IntentPromptReq(String prompt, int memoryId) {
        super();
        this.setPrompt(prompt);
        this.setMemoryId(memoryId);
    }
}