package com.huawei.chataidesign.controller;

import com.huawei.chataidesign.entity.request.IntentPromptReq;
import com.huawei.chataidesign.entity.request.PromptReq;
import com.huawei.chataidesign.entity.response.CommonResponse;
import com.huawei.chataidesign.service.AiChatService;
import com.huawei.chataidesign.service.EnhancedAiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;


@RestController
@RequestMapping("/api/ai")
@Slf4j
public class AIStreamController {
    @Resource
    private AiChatService aiChatService;

    @Resource
    private EnhancedAiChatService enhancedAiChatService;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "AI流式聊天", description = "传统的流式AI聊天接口")
    public Flux<ServerSentEvent<String>> streamAIResponseWithChatService(
            @RequestBody @Parameter(description = "聊天请求") PromptReq promptReq) {
        log.info("Received AI stream request with prompt: {}", promptReq.getPrompt());
        return aiChatService.chatWithStream(promptReq.getMemoryId(), promptReq.getPrompt())
                .map(chunk -> ServerSentEvent
                        .<String>builder()
                        .data(chunk)
                        .build())
                .timeout(Duration.ofMinutes(5)) // 设置较长的超时时间
                .onErrorResume(throwable -> {
                    log.error("Error during streaming", throwable);
                    return Flux.just(ServerSentEvent.<String>builder()
                            .event("error")
                            .data("Error occurred during streaming: " + throwable.getMessage())
                            .build());
                });
    }
    
    @PostMapping(value = "/stream-with-intent", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "带意图识别的AI流式聊天", description = "集成意图识别的流式AI聊天接口")
    public Flux<ServerSentEvent<String>> streamAIResponseWithIntentRecognition(
            @RequestBody @Parameter(description = "带意图的聊天请求") IntentPromptReq intentPromptReq) {
        log.info("Received intent-aware AI stream request with prompt: {}", intentPromptReq.getPrompt());
        
        // 如果启用了意图识别，则使用增强版服务
        if (intentPromptReq.getEnableIntentRecognition() != null && 
            intentPromptReq.getEnableIntentRecognition()) {
            return enhancedAiChatService.chatWithIntentRecognition(intentPromptReq)
                    .map(chunk -> ServerSentEvent
                            .<String>builder()
                            .data(chunk)
                            .build())
                    .timeout(Duration.ofMinutes(5))
                    .onErrorResume(throwable -> {
                        log.error("Error during intent-aware streaming", throwable);
                        return Flux.just(ServerSentEvent.<String>builder()
                                .event("error")
                                .data("Error occurred during intent-aware streaming: " + throwable.getMessage())
                                .build());
                    });
        } else {
            // 否则回退到普通聊天
            return aiChatService.chatWithStream(intentPromptReq.getMemoryId(), intentPromptReq.getPrompt())
                    .map(chunk -> ServerSentEvent
                            .<String>builder()
                            .data(chunk)
                            .build())
                    .timeout(Duration.ofMinutes(5))
                    .onErrorResume(throwable -> {
                        log.error("Error during streaming", throwable);
                        return Flux.just(ServerSentEvent.<String>builder()
                                .event("error")
                                .data("Error occurred during streaming: " + throwable.getMessage())
                                .build());
                    });
        }
    }
    
    @GetMapping("/intent-stats")
    @Operation(summary = "获取意图识别统计", description = "获取意图识别服务的使用统计")
    public org.springframework.http.ResponseEntity<CommonResponse<String>> getIntentStats() {
        try {
            String stats = enhancedAiChatService.getIntentRecognitionStats();
            CommonResponse<String> response =
                com.huawei.chataidesign.entity.response.IntentResponse.success(stats);
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get intent statistics", e);
            com.huawei.chataidesign.entity.response.IntentResponse<String> errorResponse = 
                new com.huawei.chataidesign.entity.response.IntentResponse<>(500, "获取统计失败: " + e.getMessage());
            return org.springframework.http.ResponseEntity.status(500).body(errorResponse);
        }
    }
}
