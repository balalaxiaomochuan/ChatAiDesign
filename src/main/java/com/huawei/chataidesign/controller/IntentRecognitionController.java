package com.huawei.chataidesign.controller;

import com.huawei.chataidesign.entity.IntentClassification;
import com.huawei.chataidesign.entity.request.IntentPromptReq;
import com.huawei.chataidesign.entity.response.IntentResponse;
import com.huawei.chataidesign.service.EnhancedAiChatService;
import com.huawei.chataidesign.service.IntentRecognitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 意图识别控制器
 * 提供意图识别相关的API接口
 */
@RestController
@RequestMapping("/api/intent")
@Slf4j
@Tag(name = "意图识别API", description = "提供意图识别相关功能")
public class IntentRecognitionController {
    
    @Resource
    private IntentRecognitionService intentRecognitionService;
    
    @Resource
    private EnhancedAiChatService enhancedAiChatService;
    
    @PostMapping("/recognize")
    @Operation(summary = "意图识别", description = "识别用户输入的意图类型")
    public ResponseEntity<IntentResponse<IntentClassification>> recognizeIntent(
            @RequestBody @Parameter(description = "意图识别请求") IntentPromptReq promptReq) {
        
        try {
            log.info("Received intent recognition request: {}", promptReq.getPrompt());
            
            IntentClassification result = intentRecognitionService.recognizeIntent(promptReq);
            
            IntentResponse<IntentClassification> response = IntentResponse.successIntentOnly(result);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to recognize intent", e);
            IntentResponse<IntentClassification> errorResponse = 
                new IntentResponse<>(500, "意图识别失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @PostMapping("/batch-recognize")
    @Operation(summary = "批量意图识别", description = "批量识别多个用户输入的意图")
    public ResponseEntity<IntentResponse<IntentClassification[]>> batchRecognizeIntents(
            @RequestBody @Parameter(description = "用户输入数组") String[] userInputs) {
        
        try {
            log.info("Received batch intent recognition request with {} inputs", userInputs.length);
            
            IntentClassification[] results = intentRecognitionService.recognizeIntents(userInputs);
            
            IntentResponse<IntentClassification[]> response = 
                new IntentResponse<>(200, "批量意图识别完成", results);
            response.setIntentProcessed(true);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to batch recognize intents", e);
            IntentResponse<IntentClassification[]> errorResponse = 
                new IntentResponse<>(500, "批量意图识别失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @GetMapping("/types")
    @Operation(summary = "获取支持的意图类型", description = "返回系统支持的所有意图类型及其描述")
    public ResponseEntity<IntentResponse<String>> getIntentTypes() {
        try {
            String intentTypes = com.huawei.chataidesign.entity.IntentType.getAllIntentDescriptions();
            IntentResponse<String> response = (IntentResponse<String>) IntentResponse.success(intentTypes);
            response.setIntentProcessed(false);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get intent types", e);
            IntentResponse<String> errorResponse = 
                new IntentResponse<>(500, "获取意图类型失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "获取意图识别统计", description = "返回意图识别服务的统计信息")
    public ResponseEntity<IntentResponse<String>> getStatistics() {
        try {
            String stats = intentRecognitionService.getStatistics();
            IntentResponse<String> response = (IntentResponse<String>) IntentResponse.success(stats);
            response.setIntentProcessed(false);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get statistics", e);
            IntentResponse<String> errorResponse = 
                new IntentResponse<>(500, "获取统计信息失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @DeleteMapping("/cache")
    @Operation(summary = "清除意图识别缓存", description = "清除意图识别服务的缓存数据")
    public ResponseEntity<IntentResponse<String>> clearCache() {
        try {
            intentRecognitionService.clearCache();
            IntentResponse<String> response = (IntentResponse<String>) IntentResponse.success("缓存清除成功");
            response.setIntentProcessed(false);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to clear cache", e);
            IntentResponse<String> errorResponse = 
                new IntentResponse<>(500, "清除缓存失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @PostMapping(value = "/chat-with-intent", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "带意图识别的AI聊天", description = "结合意图识别的流式AI聊天服务")
    public org.springframework.http.codec.ServerSentEvent<String> chatWithIntentRecognition(
            @RequestBody IntentPromptReq promptReq) {
        
        try {
            log.info("Received intent-aware chat request: {}", promptReq.getPrompt());
            
            return org.springframework.http.codec.ServerSentEvent.<String>builder()
                .data("开始处理您的请求...")
                .build();
                
        } catch (Exception e) {
            log.error("Failed to process intent-aware chat request", e);
            return org.springframework.http.codec.ServerSentEvent.<String>builder()
                .event("error")
                .data("处理失败: " + e.getMessage())
                .build();
        }
    }
}