package com.huawei.chataidesign.service;

import com.huawei.chataidesign.entity.IntentClassification;
import com.huawei.chataidesign.entity.request.IntentPromptReq;
import reactor.core.publisher.Flux;

/**
 * 增强版AI聊天服务接口
 * 在原有基础上增加意图识别功能
 */
public interface EnhancedAiChatService extends AiChatService {
    
    /**
     * 带意图识别的聊天服务
     * @param promptReq 意图提示请求
     * @return 包含意图识别结果的响应Flux
     */
    Flux<String> chatWithIntentRecognition(IntentPromptReq promptReq);
    
    /**
     * 仅进行意图识别
     * @param promptReq 意图提示请求
     * @return 意图分类结果
     */
    IntentClassification recognizeIntentOnly(IntentPromptReq promptReq);
    
    /**
     * 根据意图类型定制化聊天
     * @param intentClassification 意图分类结果
     * @param originalMessage 原始消息
     * @return 定制化的AI回复
     */
    String chatWithIntentCustomization(IntentClassification intentClassification, String originalMessage);
    
    /**
     * 获取意图识别统计数据
     * @return 统计信息
     */
    String getIntentRecognitionStats();
}