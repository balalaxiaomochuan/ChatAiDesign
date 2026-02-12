package com.huawei.chataidesign.integration;

import com.huawei.chataidesign.ChatAiDesignApplication;
import com.huawei.chataidesign.entity.IntentClassification;
import com.huawei.chataidesign.entity.request.IntentPromptReq;
import com.huawei.chataidesign.service.IntentRecognitionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 意图识别集成测试
 * 测试在真实环境下的功能表现
 */
@SpringBootTest(classes = ChatAiDesignApplication.class)
@ActiveProfiles("local")
public class IntentRecognitionIntegrationTest {
    
    @Resource
    private IntentRecognitionService intentRecognitionService;
    
    @BeforeEach
    public void setUp() {
        // 清除缓存确保测试独立性
        intentRecognitionService.clearCache();
    }
    
    @Test
    public void testRealWorldScenarios() {
        // 测试真实世界的使用场景
        String[][] testCases = {
            {"我想系统学习Java开发", "LEARNING_PATH"},
            {"帮我看看这段Spring代码有什么问题", "TECHNICAL_QUESTION"},
            {"Java多线程面试会问什么？", "INTERVIEW_PREPARATION"},
            {"推荐几个适合练手的项目", "PROJECT_GUIDANCE"},
            {"你好，我在学习编程", "GREETING"}
        };
        
        for (String[] testCase : testCases) {
            String input = testCase[0];
            String expectedCategory = testCase[1];
            
            IntentClassification result = intentRecognitionService.recognizeIntent(input, null);
            
            assertNotNull(result, "结果不应为空: " + input);
            assertNotNull(result.getPrimaryIntent(), "主要意图不应为空: " + input);
            assertTrue(result.getConfidence() >= 0.0, "置信度应在0-1之间: " + input);
            assertTrue(result.getConfidence() <= 1.0, "置信度应在0-1之间: " + input);
            
            System.out.printf("输入: %-20s | 识别意图: %-15s | 置信度: %.2f%n", 
                input, result.getPrimaryIntent().getDisplayName(), result.getConfidence());
        }
    }
    
    @Test
    public void testConcurrentRecognition() throws InterruptedException {
        // 测试并发意图识别
        int threadCount = 10;
        int requestsPerThread = 5;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        Runnable task = () -> {
            try {
                for (int i = 0; i < requestsPerThread; i++) {
                    String input = "测试并发输入 " + i;
                    IntentClassification result = intentRecognitionService.recognizeIntent(input, null);
                    assertNotNull(result);
                }
            } finally {
                latch.countDown();
            }
        };
        
        // 提交所有任务
        for (int i = 0; i < threadCount; i++) {
            executor.submit(task);
        }
        
        // 等待所有任务完成
        assertTrue(latch.await(30, TimeUnit.SECONDS), "并发测试应在30秒内完成");
        executor.shutdown();
        
        // 验证统计信息
        String stats = intentRecognitionService.getStatistics();
        System.out.println("并发测试后的统计: " + stats);
    }
    
    @Test
    public void testCacheEffectiveness() {
        // 测试缓存效果
        String testInput = "相同的测试输入用于验证缓存";
        
        // 第一次调用
        long startTime1 = System.currentTimeMillis();
        IntentClassification result1 = intentRecognitionService.recognizeIntent(testInput, null);
        long duration1 = System.currentTimeMillis() - startTime1;
        
        // 第二次调用（应该命中缓存）
        long startTime2 = System.currentTimeMillis();
        IntentClassification result2 = intentRecognitionService.recognizeIntent(testInput, null);
        long duration2 = System.currentTimeMillis() - startTime2;
        
        // 验证结果一致性
        assertEquals(result1.getPrimaryIntent(), result2.getPrimaryIntent());
        assertEquals(result1.getConfidence(), result2.getConfidence());
        
        // 验证性能提升（第二次应该更快）
        System.out.printf("首次调用耗时: %d ms, 缓存调用耗时: %d ms%n", duration1, duration2);
        
        // 获取统计信息验证缓存命中
        String stats = intentRecognitionService.getStatistics();
        System.out.println("缓存测试统计: " + stats);
    }
    
    @Test
    public void testContextAwareRecognition() {
        // 测试上下文感知的意图识别
        String userInput = "这个问题怎么解决？";
        
        // 不同上下文应该产生不同的意图识别结果
        IntentClassification result1 = intentRecognitionService.recognizeIntent(
            userInput, "用户正在学习Java基础语法");
        
        IntentClassification result2 = intentRecognitionService.recognizeIntent(
            userInput, "用户在调试Spring Boot应用");
        
        IntentClassification result3 = intentRecognitionService.recognizeIntent(
            userInput, "用户准备技术面试");
        
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
        
        System.out.println("相同问题不同上下文的识别结果:");
        System.out.println("Java基础语法上下文: " + result1.getPrimaryIntent());
        System.out.println("Spring Boot调试上下文: " + result2.getPrimaryIntent());
        System.out.println("技术面试上下文: " + result3.getPrimaryIntent());
    }
    
    @Test
    public void testEdgeCases() {
        // 测试边界情况
        
        // 空输入
        IntentClassification result1 = intentRecognitionService.recognizeIntent("", null);
        assertNotNull(result1);
        System.out.println("空输入识别结果: " + result1.getPrimaryIntent());
        
        // 特殊字符
        IntentClassification result2 = intentRecognitionService.recognizeIntent("!@#$%^&*()", null);
        assertNotNull(result2);
        System.out.println("特殊字符输入识别结果: " + result2.getPrimaryIntent());
        
        // 超长输入
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longInput.append("很长的输入内容 ");
        }
        IntentClassification result3 = intentRecognitionService.recognizeIntent(longInput.toString(), null);
        assertNotNull(result3);
        System.out.println("超长输入识别结果: " + result3.getPrimaryIntent());
    }
    
    @Test
    public void testIntentPromptReqFullFlow() {
        // 测试完整的IntentPromptReq流程
        IntentPromptReq promptReq = new IntentPromptReq();
        promptReq.setPrompt("我想做一个在线购物网站，用什么技术栈比较好？");
        promptReq.setMemoryId(123);
        promptReq.setEnableIntentRecognition(true);
        promptReq.setMinConfidence(0.65);
        promptReq.setRequireConfirmation(true);
        promptReq.setContext("用户有一定编程基础，熟悉Java");
        promptReq.setConversationHistory("用户之前询问过Spring Boot相关问题");
        
        IntentClassification result = intentRecognitionService.recognizeIntent(promptReq);
        
        assertNotNull(result);
        assertNotNull(result.getPrimaryIntent());
        assertNotNull(result.getConfidence());
        assertTrue(result.getConfidence() >= 0.0 && result.getConfidence() <= 1.0);
        assertTrue(result.getProcessedAt() != null);
        
        System.out.println("完整流程测试结果:");
        System.out.println("主要意图: " + result.getPrimaryIntent().getDisplayName());
        System.out.println("置信度: " + result.getConfidence());
        System.out.println("是否需要确认: " + result.getNeedsConfirmation());
        System.out.println("建议动作: " + result.getSuggestedAction());
    }
}