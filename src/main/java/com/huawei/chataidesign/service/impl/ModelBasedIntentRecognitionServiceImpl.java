package com.huawei.chataidesign.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.chataidesign.entity.IntentClassification;
import com.huawei.chataidesign.entity.IntentType;
import com.huawei.chataidesign.entity.request.IntentPromptReq;
import com.huawei.chataidesign.service.IntentRecognitionService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.model.chat.ChatModel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 意图识别服务接口
 * 使用LangChain4j的注解方式调用大模型
 */
interface IntentRecognitionAiService {
    @SystemMessage("你是一个专业的意图分类助手。你的任务是根据用户输入，识别其主要意图。请以JSON格式返回分类结果，包含以下字段：intentCode（意图编码）、intentName（意图名称）、confidence（置信度0.0-1.0）、intentDescription（意图描述）、suggestedAction（建议的后续动作）、needsConfirmation（是否需要进一步确认）、entities（提取的关键实体，可选）。")
    String recognizeIntent(@UserMessage String userInput);
}

/**
 * 基于大模型的意图识别服务实现
 * 使用AI模型进行意图分类，支持结构化输出
 */
@Slf4j
@Service
public class ModelBasedIntentRecognitionServiceImpl implements IntentRecognitionService {

    @Resource(name = "qwenChatModel")
    private ChatModel chatModel;

    private IntentRecognitionAiService aiService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ObjectMapper objectMapper;
    
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
        // 使用AiServices创建意图识别服务
        aiService = AiServices.builder(IntentRecognitionAiService.class)
                .chatModel(chatModel)
                .build();
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

        // 使用大模型进行意图识别
        IntentClassification result = performModelBasedRecognition(userInput);

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
     * 基于大模型的意图识别
     */
    private IntentClassification performModelBasedRecognition(String userInput) {
        modelCalls.incrementAndGet();

        IntentClassification classification = new IntentClassification();
        classification.setIntentId(UUID.randomUUID().toString());
        classification.setUserInput(userInput);
        classification.setProcessedAt(LocalDateTime.now());

        try {
            // 构建完整提示词，包含意图类型信息
            String fullPrompt = buildFullPrompt(userInput);

            // 使用AiServices调用大模型
            String jsonResponse = aiService.recognizeIntent(fullPrompt);

            log.info("Model response for intent recognition: {}", jsonResponse);

            // 解析JSON响应
            classification = parseJsonResponse(jsonResponse, userInput);

        } catch (Exception e) {
            log.error("Error during model-based intent recognition: {}", e.getMessage(), e);
            // 降级处理：返回OTHER意图
            classification.setPrimaryIntent(IntentType.OTHER);
            classification.setConfidence(0.3);
            classification.setIntentDescription("意图识别失败，降级处理");
            classification.setSuggestedAction("已收到您的消息，正在为您处理");
        }

        return classification;
    }

    /**
     * 构建完整提示词
     */
    private String buildFullPrompt(String userInput) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个专业的意图分类助手。你的任务是根据用户输入，识别其主要意图。\n\n");
        sb.append("支持的意图类型如下：\n");
        for (IntentType intentType : IntentType.values()) {
            sb.append(String.format("- %s (%s): %s\n",
                    intentType.getDisplayName(),
                    intentType.getCode(),
                    intentType.getDescription()));
        }
        sb.append("\n请以JSON格式返回分类结果，包含以下字段：\n");
        sb.append("- intentCode: 意图编码（code）\n");
        sb.append("- intentName: 意图名称（displayName）\n");
        sb.append("- confidence: 置信度（0.0-1.0之间的数值）\n");
        sb.append("- intentDescription: 意图描述\n");
        sb.append("- suggestedAction: 建议的后续动作\n");
        sb.append("- needsConfirmation: 是否需要进一步确认（true/false）\n");
        sb.append("- entities: 提取的关键实体（可选）\n");
        sb.append("\n用户输入：\n");
        sb.append(userInput);
        sb.append("\n\n请只返回JSON，不要包含其他内容。");
        return sb.toString();
    }

    /**
     * 解析大模型返回的JSON响应
     */
    private IntentClassification parseJsonResponse(String jsonResponse, String userInput) {
        try {
            // 提取JSON内容（去除可能的markdown代码块标记）
            String cleanJson = extractJsonContent(jsonResponse);

            Map<String, Object> responseMap = objectMapper.readValue(cleanJson, Map.class);

            IntentClassification classification = new IntentClassification();
            classification.setIntentId(UUID.randomUUID().toString());
            classification.setUserInput(userInput);
            classification.setProcessedAt(LocalDateTime.now());

            // 解析意图编码
            String intentCode = (String) responseMap.get("intentCode");
            classification.setPrimaryIntent(IntentType.fromCode(intentCode));

            // 解析置信度
            Object confidenceObj = responseMap.get("confidence");
            if (confidenceObj instanceof Number) {
                classification.setConfidence(((Number) confidenceObj).doubleValue());
            } else {
                classification.setConfidence(0.8);
            }

            // 解析其他字段
            classification.setIntentDescription((String) responseMap.get("intentDescription"));
            classification.setSuggestedAction((String) responseMap.get("suggestedAction"));
            classification.setEntities(responseMap.get("entities").toString());

            Object needsConfirm = responseMap.get("needsConfirmation");
            classification.setNeedsConfirmation(needsConfirm instanceof Boolean ? (Boolean) needsConfirm : false);

            log.info("Parsed intent classification: {} with confidence: {}",
                    classification.getPrimaryIntent(), classification.getConfidence());

            return classification;

        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON response: {}", jsonResponse, e);
            // 降级处理
            return createFallbackClassification(userInput, "JSON解析失败");
        }
    }

    /**
     * 提取JSON内容，去除可能的markdown代码块标记
     */
    private String extractJsonContent(String response) {
        String trimmed = response.trim();
        // 去除 ```json 开头和 ``` 结尾
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }

    /**
     * 创建降级分类结果
     */
    private IntentClassification createFallbackClassification(String userInput, String reason) {
        IntentClassification classification = new IntentClassification();
        classification.setIntentId(UUID.randomUUID().toString());
        classification.setUserInput(userInput);
        classification.setProcessedAt(LocalDateTime.now());
        classification.setPrimaryIntent(IntentType.OTHER);
        classification.setConfidence(0.3);
        classification.setIntentDescription("识别失败: " + reason);
        classification.setSuggestedAction("已收到您的消息，正在为您处理");
        classification.setNeedsConfirmation(true);
        return classification;
    }
}