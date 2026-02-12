package com.huawei.chataidesign.entity.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.huawei.chataidesign.entity.IntentClassification;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 带意图识别的响应实体
 * 在原有响应基础上增加意图识别结果
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IntentResponse<T> extends CommonResponse<T> {
    
    /**
     * 意图识别结果
     */
    @JsonProperty("intent_classification")
    private IntentClassification intentClassification;
    
    /**
     * 是否进行了意图识别
     */
    @JsonProperty("intent_processed")
    private Boolean intentProcessed;
    
    /**
     * 处理建议
     */
    @JsonProperty("processing_suggestion")
    private String processingSuggestion;
    
    public IntentResponse() {
        super();
        this.intentProcessed = false;
    }
    
    public IntentResponse(Integer code, String message) {
        super(code, message);
        this.intentProcessed = false;
    }
    
    public IntentResponse(Integer code, String message, T data) {
        super(code, message, data);
        this.intentProcessed = false;
    }
    
    /**
     * 成功响应（带意图识别结果）
     */
    public static <T> IntentResponse<T> successWithIntent(T data, IntentClassification intentClassification) {
        IntentResponse<T> response = new IntentResponse<>(200, "Success", data);
        response.setIntentClassification(intentClassification);
        response.setIntentProcessed(true);
        response.setProcessingSuggestion(generateSuggestion(intentClassification));
        return response;
    }
    
    /**
     * 成功响应（仅意图识别）
     */
    public static <T> IntentResponse<T> successIntentOnly(IntentClassification intentClassification) {
        IntentResponse<T> response = new IntentResponse<>(200, "Intent recognized successfully");
        response.setIntentClassification(intentClassification);
        response.setIntentProcessed(true);
        response.setProcessingSuggestion(generateSuggestion(intentClassification));
        return response;
    }
    
    /**
     * 根据意图类型生成处理建议
     */
    private static String generateSuggestion(IntentClassification intentClassification) {
        if (intentClassification == null) {
            return "无法识别用户意图，请重新表述";
        }
        
        switch (intentClassification.getPrimaryIntent()) {
            case LEARNING_PATH:
                return "检测到学习路线咨询，将为您提供个性化的学习建议";
            case PROJECT_GUIDANCE:
                return "检测到项目指导需求，将为您推荐合适的项目方案";
            case TECHNICAL_QUESTION:
                return "检测到技术问题，将为您提供专业的技术解答";
            case INTERVIEW_PREPARATION:
                return "检测到面试准备需求，将为您提供面试指导";
            case GREETING:
                return "您好！我是您的编程学习助手，有什么可以帮助您的吗？";
            case UNCLEAR:
                return "您的意图不够明确，建议您提供更多详细信息";
            default:
                return "已识别您的需求，正在为您准备相关回答";
        }
    }
}