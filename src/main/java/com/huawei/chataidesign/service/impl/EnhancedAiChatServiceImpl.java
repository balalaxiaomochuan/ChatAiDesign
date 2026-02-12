package com.huawei.chataidesign.service.impl;

import com.huawei.chataidesign.entity.IntentClassification;
import com.huawei.chataidesign.entity.IntentType;
import com.huawei.chataidesign.entity.request.IntentPromptReq;
import com.huawei.chataidesign.service.AiChatService;
import com.huawei.chataidesign.service.EnhancedAiChatService;
import com.huawei.chataidesign.service.IntentRecognitionService;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * 增强版AI聊天服务实现
 * 集成意图识别功能
 */
@Slf4j
@Service
public class EnhancedAiChatServiceImpl implements EnhancedAiChatService {
    @Resource
    private AiChatService aiChatService;
    
    @Resource
    private IntentRecognitionService intentRecognitionService;
    
    @Value("${intent.recognition.enabled:true}")
    private boolean intentRecognitionEnabled;
    
    @Override
    @SystemMessage(fromResource = "system-prompt.txt")
    public String chat(String message) {
        return chat(message);
    }
    
    @Override
    @SystemMessage(fromResource = "system-prompt.txt")
    public Flux<String> chatWithStream(@MemoryId int memoryId, @UserMessage String message) {
        // 如果启用了意图识别，则先进行意图识别再聊天
        if (intentRecognitionEnabled) {
            IntentPromptReq promptReq = new IntentPromptReq(message, memoryId);
            IntentClassification intent = intentRecognitionService.recognizeIntent(promptReq);
            
            // 根据意图定制系统提示
            String customizedMessage = customizeMessageByIntent(intent, message);
            log.info("Intent recognized: {} with confidence: {}, customized message: {}", 
                    intent.getPrimaryIntent(), intent.getConfidence(), customizedMessage);
            
            return aiChatService.chatWithStream(memoryId, customizedMessage);
        }
        
        // 原有的流式聊天逻辑
        return aiChatService.chatWithStream(memoryId, message);
    }
    
    @Override
    public Flux<String> chatWithIntentRecognition(IntentPromptReq promptReq) {
        if (!intentRecognitionEnabled) {
            log.warn("Intent recognition is disabled, falling back to normal chat");
            return chatWithStream(promptReq.getMemoryId(), promptReq.getPrompt());
        }
        
        try {
            // 进行意图识别
            IntentClassification intent = intentRecognitionService.recognizeIntent(promptReq);
            log.info("Intent recognized: {} with confidence: {}", 
                    intent.getPrimaryIntent(), intent.getConfidence());
            
            // 根据意图定制消息
            String customizedMessage = customizeMessageByIntent(intent, promptReq.getPrompt());
            
            // 返回包含意图信息的流式响应
            return Flux.concat(
                Flux.just("[意图识别] 主要意图: " + intent.getPrimaryIntent().getDisplayName()),
                Flux.just("[置信度] " + String.format("%.2f", intent.getConfidence())),
                Flux.just("[建议动作] " + intent.getSuggestedAction()),
                Flux.just("---"),
                chatWithStream(promptReq.getMemoryId(), customizedMessage)
            ).timeout(Duration.ofMinutes(5))
             .onErrorResume(throwable -> {
                 log.error("Error in intent-aware chat", throwable);
                 return Flux.just("抱歉，在处理您的请求时发生了错误: " + throwable.getMessage());
             });
             
        } catch (Exception e) {
            log.error("Failed to perform intent recognition chat", e);
            return Flux.just("意图识别服务暂时不可用，请稍后重试");
        }
    }
    
    @Override
    public IntentClassification recognizeIntentOnly(IntentPromptReq promptReq) {
        if (!intentRecognitionEnabled) {
            throw new IllegalStateException("意图识别功能未启用");
        }
        
        return intentRecognitionService.recognizeIntent(promptReq);
    }
    
    @Override
    @SystemMessage(fromResource = "system-prompt.txt")
    public String chatWithIntentCustomization(IntentClassification intentClassification, String originalMessage) {
        String customizedMessage = customizeMessageByIntent(intentClassification, originalMessage);
        return chat(customizedMessage);
    }
    
    @Override
    public String getIntentRecognitionStats() {
        if (!intentRecognitionEnabled) {
            return "意图识别功能未启用";
        }
        return intentRecognitionService.getStatistics();
    }
    
    /**
     * 根据意图类型定制消息
     */
    private String customizeMessageByIntent(IntentClassification intent, String originalMessage) {
        if (intent == null || intent.getPrimaryIntent() == null) {
            return originalMessage;
        }
        
        StringBuilder customizedMessage = new StringBuilder();
        
        switch (intent.getPrimaryIntent()) {
            case LEARNING_PATH:
                customizedMessage.append("[学习咨询] ");
                customizedMessage.append("用户希望了解学习路线。请提供结构化的学习建议，包括阶段划分、重点知识点和技术栈推荐。\n\n");
                break;
                
            case PROJECT_GUIDANCE:
                customizedMessage.append("[项目指导] ");
                customizedMessage.append("用户需要项目开发建议。请推荐合适的项目类型、技术选型和实现思路。\n\n");
                break;
                
            case TECHNICAL_QUESTION:
                customizedMessage.append("[技术问题] ");
                customizedMessage.append("用户提出了具体的技术问题。请提供准确、详细的解答，并给出示例代码。\n\n");
                break;
                
            case INTERVIEW_PREPARATION:
                customizedMessage.append("[面试准备] ");
                customizedMessage.append("用户在准备面试。请提供面试重点、常见问题和答题技巧。\n\n");
                break;
                
            case GREETING:
                customizedMessage.append("[问候] ");
                customizedMessage.append("用户在打招呼。请友好回应并引导用户说明具体需求。\n\n");
                break;
                
            case UNCLEAR:
                customizedMessage.append("[意图不明确] ");
                customizedMessage.append("用户意图不够清晰。请礼貌地询问更多细节信息。\n\n");
                break;
                
            default:
                // 其他意图保持原消息
                return originalMessage;
        }
        
        customizedMessage.append("原始问题: ").append(originalMessage);
        return customizedMessage.toString();
    }
}