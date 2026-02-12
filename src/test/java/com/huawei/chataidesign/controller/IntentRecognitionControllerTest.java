package com.huawei.chataidesign.controller;

import com.huawei.chataidesign.entity.request.IntentPromptReq;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 意图识别控制器测试类
 */
@SpringBootTest
@AutoConfigureMockMvc
public class IntentRecognitionControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    public void testRecognizeIntent() throws Exception {
        String requestBody = """
            {
                "prompt": "我想学习Spring Boot，应该如何入门？",
                "memory_id": 1,
                "enable_intent_recognition": true,
                "min_confidence": 0.7
            }
            """;
        
        mockMvc.perform(post("/api/intent/recognize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.intent_processed").value(true))
                .andExpect(jsonPath("$.intent_classification").exists())
                .andExpect(jsonPath("$.intent_classification.primary_intent").exists());
    }
    
    @Test
    public void testGetIntentTypes() throws Exception {
        mockMvc.perform(get("/api/intent/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }
    
    @Test
    public void testGetStatistics() throws Exception {
        mockMvc.perform(get("/api/intent/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }
    
    @Test
    public void testBatchRecognizeIntents() throws Exception {
        String requestBody = """
            [
                "如何学习Java？",
                "Spring框架有什么特点？",
                "面试准备建议"
            ]
            """;
        
        mockMvc.perform(post("/api/intent/batch-recognize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.intent_processed").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
    
    @Test
    public void testClearCache() throws Exception {
        mockMvc.perform(delete("/api/intent/cache"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("缓存清除成功"));
    }
    
    @Test
    public void testChatWithIntentRecognition() throws Exception {
        String requestBody = """
            {
                "prompt": "帮我推荐一个适合新手的Java项目",
                "memory_id": 1,
                "enable_intent_recognition": true,
                "min_confidence": 0.6
            }
            """;
        
        mockMvc.perform(post("/api/ai/stream-with-intent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }
    
    @Test
    public void testGetIntentStats() throws Exception {
        mockMvc.perform(get("/api/ai/intent-stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }
    
    @Test
    public void testInvalidInput() throws Exception {
        String requestBody = """
            {
                "prompt": "",
                "memory_id": 1
            }
            """;
        
        mockMvc.perform(post("/api/intent/recognize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk()); // 应该能够处理空输入
    }
    
    @Test
    public void testLongInput() throws Exception {
        // 测试长输入处理
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            longInput.append("测试输入 ");
        }
        
        IntentPromptReq req = new IntentPromptReq();
        req.setPrompt(longInput.toString());
        req.setMemoryId(1);
        
        String requestBody = String.format("""
            {
                "prompt": "%s",
                "memory_id": 1
            }
            """, longInput.toString().replace("\"", "\\\""));
        
        mockMvc.perform(post("/api/intent/recognize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }
}