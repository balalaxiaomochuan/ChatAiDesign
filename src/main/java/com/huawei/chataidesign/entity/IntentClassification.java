package com.huawei.chataidesign.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 意图分类结果实体类
 * 用于存储用户输入的意图识别结果
 */
@Data
public class IntentClassification implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 意图ID
     */
    private String intentId;
    
    /**
     * 主要意图类别
     */
    private IntentType primaryIntent;
    
    /**
     * 置信度 (0.0 - 1.0)
     */
    private Double confidence;
    
    /**
     * 原始用户输入
     */
    private String userInput;
    
    /**
     * 识别出的具体意图描述
     */
    private String intentDescription;
    
    /**
     * 相关的实体信息
     */
    private String entities;
    
    /**
     * 处理时间
     */
    private LocalDateTime processedAt;
    
    /**
     * 是否需要进一步确认
     */
    private Boolean needsConfirmation;
    
    /**
     * 建议的后续动作
     */
    private String suggestedAction;
    
    public IntentClassification() {
        this.processedAt = LocalDateTime.now();
        this.needsConfirmation = false;
    }
    
    public IntentClassification(IntentType primaryIntent, Double confidence, String userInput) {
        this();
        this.primaryIntent = primaryIntent;
        this.confidence = confidence;
        this.userInput = userInput;
    }
}