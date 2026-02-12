package com.huawei.chataidesign.service.impl;

import com.huawei.chataidesign.entity.IntentClassification;
import com.huawei.chataidesign.entity.IntentType;
import com.huawei.chataidesign.entity.request.IntentPromptReq;
import com.huawei.chataidesign.service.IntentRecognitionService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 简化版意图识别服务实现
 * 使用关键词匹配的临时解决方案
 */
@Slf4j
@Service
public class ModelBasedIntentRecognitionServiceImpl implements IntentRecognitionService {
    
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    @Value("${intent.recognition.cache.enabled:true}")
    private boolean cacheEnabled;
    
    @Value("${intent.recognition.cache.ttl-minutes:30}")
    private int cacheTtlMinutes;
    
    @Value("${intent.recognition.default-confidence-threshold:0.7}")
    private double defaultConfidenceThreshold;
    
    // 统计计数器
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong modelCalls = new AtomicLong(0);
    
    @PostConstruct
    public void init() {
        log.info("ModelBasedIntentRecognitionService initialized with cache={}, ttl={} minutes", 
                cacheEnabled, cacheTtlMinutes);
    }
    
    @Override
    public IntentClassification recognizeIntent(String userInput, String context) {
        IntentPromptReq promptReq = new IntentPromptReq();
        promptReq.setPrompt(userInput);
        promptReq.setContext(context);
        promptReq.setMinConfidence(defaultConfidenceThreshold);
        
        return recognizeIntent(promptReq);
    }
    
    @Override
    public IntentClassification recognizeIntent(IntentPromptReq promptReq) {
        totalRequests.incrementAndGet();
        String userInput = promptReq.getPrompt();
        
        // 简单的关键词匹配实现（临时方案），当前使用的是关键词匹配，企业级项目中可能会用机器学习、神经网络来做文本分类，泛化性会更好
        IntentClassification result = performKeywordBasedRecognition(userInput);
        
        return result;
    }
    
    @Override
    public IntentClassification[] recognizeIntents(String[] userInputs) {
        IntentClassification[] results = new IntentClassification[userInputs.length];
        for (int i = 0; i < userInputs.length; i++) {
            results[i] = recognizeIntent(userInputs[i], null);
        }
        return results;
    }
    
    @Override
    public boolean validateConfidence(IntentClassification intentClassification, double minConfidence) {
        return intentClassification != null && 
               intentClassification.getConfidence() != null &&
               intentClassification.getConfidence() >= minConfidence;
    }
    
    @Override
    public String getStatistics() {
        long total = totalRequests.get();
        long hits = cacheHits.get();
        long calls = modelCalls.get();
        double cacheHitRate = total > 0 ? (double) hits / total * 100 : 0;
        
        return String.format(
            "意图识别统计 - 总请求数: %d, 缓存命中: %d, 模型调用: %d, 缓存命中率: %.2f%%",
            total, hits, calls, cacheHitRate
        );
    }
    
    @Override
    public void clearCache() {
        log.info("Intent recognition cache cleared");
    }
    
    /**
     * 基于关键词的简单意图识别（临时实现）
     */
    private IntentClassification performKeywordBasedRecognition(String userInput) {
        IntentClassification classification = new IntentClassification();
        classification.setIntentId(UUID.randomUUID().toString());
        classification.setUserInput(userInput);
        classification.setProcessedAt(LocalDateTime.now());
        classification.setConfidence(0.8); // 默认置信度
        classification.setNeedsConfirmation(false);
        
        String lowerInput = userInput.toLowerCase();
        
        // 简单的关键词匹配逻辑
        if (lowerInput.contains("学习") || lowerInput.contains("学") || lowerInput.contains("路线")) {
            classification.setPrimaryIntent(IntentType.LEARNING_PATH);
            classification.setIntentDescription("学习路线规划");
            classification.setSuggestedAction("检测到学习路线咨询，将为您提供个性化的学习建议");
        } else if (lowerInput.contains("项目") || lowerInput.contains("练手")) {
            classification.setPrimaryIntent(IntentType.PROJECT_GUIDANCE);
            classification.setIntentDescription("项目指导");
            classification.setSuggestedAction("检测到项目指导需求，将为您推荐合适的项目方案");
        } else if (lowerInput.contains("面试") || lowerInput.contains("求职")) {
            classification.setPrimaryIntent(IntentType.INTERVIEW_PREPARATION);
            classification.setIntentDescription("面试准备");
            classification.setSuggestedAction("检测到面试准备需求，将为您提供面试指导");
        } else if (lowerInput.contains("技术") || lowerInput.contains("问题") || lowerInput.contains("怎么") || lowerInput.contains("如何")) {
            classification.setPrimaryIntent(IntentType.TECHNICAL_QUESTION);
            classification.setIntentDescription("技术问题");
            classification.setSuggestedAction("检测到技术问题，将为您提供专业的技术解答");
        } else if (lowerInput.contains("你好") || lowerInput.contains("hello") || lowerInput.contains("hi")) {
            classification.setPrimaryIntent(IntentType.GREETING);
            classification.setIntentDescription("问候");
            classification.setSuggestedAction("您好！我是您的编程学习助手，有什么可以帮助您的吗？");
        } else {
            classification.setPrimaryIntent(IntentType.OTHER);
            classification.setIntentDescription("其他");
            classification.setSuggestedAction("已识别您的需求，正在为您准备相关回答");
        }
        
        log.debug("Keyword-based intent recognition result: {} for input: {}", 
                classification.getPrimaryIntent(), userInput);
        
        return classification;
    }
}