package com.huawei.chataidesign.service;

import com.huawei.chataidesign.entity.IntentClassification;
import com.huawei.chataidesign.entity.IntentType;
import com.huawei.chataidesign.entity.request.IntentPromptReq;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 意图识别服务测试类
 */
@SpringBootTest
public class IntentRecognitionServiceTest {
    
    @Resource
    private IntentRecognitionService intentRecognitionService;
    
    @Test
    public void testRecognizeSimpleIntent() {
        // 测试简单的学习相关意图
        String userInput = "我想学习Java编程，应该从哪里开始？";
        IntentClassification result = intentRecognitionService.recognizeIntent(userInput, null);
        
        assertNotNull(result);
        assertNotNull(result.getPrimaryIntent());
        assertTrue(result.getConfidence() >= 0.0);
        assertTrue(result.getConfidence() <= 1.0);
        assertEquals(userInput, result.getUserInput());
        
        System.out.println("识别结果: " + result.getPrimaryIntent().getDisplayName());
        System.out.println("置信度: " + result.getConfidence());
        System.out.println("描述: " + result.getIntentDescription());
    }
    
    @Test
    public void testRecognizeTechnicalQuestion() {
        // 测试技术问题意图
        String userInput = "Spring Boot中的@Autowired注解是如何工作的？";
        IntentClassification result = intentRecognitionService.recognizeIntent(userInput, null);
        
        assertNotNull(result);
        System.out.println("技术问题识别结果: " + result.getPrimaryIntent());
    }
    
    @Test
    public void testRecognizeInterviewQuestion() {
        // 测试面试相关意图
        String userInput = "Java面试经常问哪些多线程问题？";
        IntentClassification result = intentRecognitionService.recognizeIntent(userInput, null);
        
        assertNotNull(result);
        System.out.println("面试问题识别结果: " + result.getPrimaryIntent());
    }
    
    @Test
    public void testBatchRecognition() {
        // 测试批量意图识别
        String[] inputs = {
            "帮我规划一个Python学习路线",
            "Spring Cloud是什么？",
            "如何准备技术面试？"
        };
        
        IntentClassification[] results = intentRecognitionService.recognizeIntents(inputs);
        
        assertEquals(inputs.length, results.length);
        for (int i = 0; i < results.length; i++) {
            assertNotNull(results[i]);
            assertEquals(inputs[i], results[i].getUserInput());
            System.out.println("输入: " + inputs[i]);
            System.out.println("识别意图: " + results[i].getPrimaryIntent());
            System.out.println("---");
        }
    }
    
    @Test
    public void testConfidenceValidation() {
        // 测试置信度验证
        String userInput = "今天天气怎么样？";
        IntentClassification result = intentRecognitionService.recognizeIntent(userInput, null);
        
        // 测试高置信度阈值
        boolean highConfidence = intentRecognitionService.validateConfidence(result, 0.9);
        System.out.println("高置信度(0.9)验证: " + highConfidence);
        
        // 测试低置信度阈值
        boolean lowConfidence = intentRecognitionService.validateConfidence(result, 0.1);
        System.out.println("低置信度(0.1)验证: " + lowConfidence);
    }
    
    @Test
    public void testIntentPromptReq() {
        // 测试带配置的意图识别
        IntentPromptReq promptReq = new IntentPromptReq();
        promptReq.setPrompt("我想做一个电商项目，有什么建议吗？");
        promptReq.setMemoryId(1);
        promptReq.setEnableIntentRecognition(true);
        promptReq.setMinConfidence(0.6);
        promptReq.setRequireConfirmation(false);
        promptReq.setContext("用户是编程初学者");
        
        IntentClassification result = intentRecognitionService.recognizeIntent(promptReq);
        
        assertNotNull(result);
        System.out.println("带上下文的识别结果: " + result.getPrimaryIntent());
        System.out.println("上下文: " + promptReq.getContext());
    }
    
    @Test
    public void testGreetingIntent() {
        // 测试问候意图
        String userInput = "你好";
        IntentClassification result = intentRecognitionService.recognizeIntent(userInput, null);
        
        assertNotNull(result);
        System.out.println("问候识别结果: " + result.getPrimaryIntent());
    }
    
    @Test
    public void testUnclearIntent() {
        // 测试不明确意图
        String userInput = "asdfghjkl"; // 无意义输入
        IntentClassification result = intentRecognitionService.recognizeIntent(userInput, null);
        
        assertNotNull(result);
        System.out.println("不明确输入识别结果: " + result.getPrimaryIntent());
        System.out.println("是否需要确认: " + result.getNeedsConfirmation());
    }
    
    @Test
    public void testStatistics() {
        // 测试统计功能
        String stats = intentRecognitionService.getStatistics();
        assertNotNull(stats);
        System.out.println("统计信息: " + stats);
        
        // 执行几次识别后再查看统计
        intentRecognitionService.recognizeIntent("测试输入1", null);
        intentRecognitionService.recognizeIntent("测试输入2", null);
        
        String updatedStats = intentRecognitionService.getStatistics();
        System.out.println("更新后的统计信息: " + updatedStats);
    }
    
    @Test
    public void testIntentTypeEnum() {
        // 测试意图类型枚举
        System.out.println("所有支持的意图类型:");
        System.out.println(IntentType.getAllIntentDescriptions());
        
        // 测试根据编码获取意图类型
        IntentType learningPath = IntentType.fromCode("learning_path");
        assertEquals(IntentType.LEARNING_PATH, learningPath);
        
        // 测试不存在的编码
        IntentType unknown = IntentType.fromCode("unknown_code");
        assertEquals(IntentType.OTHER, unknown);
    }
}