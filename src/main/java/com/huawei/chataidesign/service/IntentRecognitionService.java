package com.huawei.chataidesign.service;

import com.huawei.chataidesign.entity.IntentClassification;
import com.huawei.chataidesign.entity.request.IntentPromptReq;

/**
 * 意图识别服务接口
 * 提供意图分类的核心功能
 */
public interface IntentRecognitionService {
    
    /**
     * 识别用户输入的意图
     * @param userInput 用户输入文本
     * @param context 上下文信息（可选）
     * @return 意图分类结果
     */
    IntentClassification recognizeIntent(String userInput, String context);
    
    /**
     * 识别用户输入的意图（带配置参数）
     * @param promptReq 意图提示请求对象
     * @return 意图分类结果
     */
    IntentClassification recognizeIntent(IntentPromptReq promptReq);
    
    /**
     * 批量识别多个用户输入的意图
     * @param userInputs 用户输入文本数组
     * @return 意图分类结果数组
     */
    IntentClassification[] recognizeIntents(String[] userInputs);
    
    /**
     * 验证意图识别结果的置信度
     * @param intentClassification 意图分类结果
     * @param minConfidence 最小置信度阈值
     * @return 是否满足置信度要求
     */
    boolean validateConfidence(IntentClassification intentClassification, double minConfidence);
    
    /**
     * 获取意图识别统计信息
     * @return 统计信息字符串
     */
    String getStatistics();
    
    /**
     * 清除意图识别缓存
     */
    void clearCache();
}