package com.huawei.chataidesign.config;

import com.huawei.chataidesign.service.AiChatService;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;

import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiChatServiceFactory {
    @Resource
    private ChatModel qwenChatModel;

    @Resource
    private ContentRetriever contentRetriever;

    @Resource
    private StreamingChatModel qwenStreamingChatModel;

    @Resource
    private McpToolProvider mcpToolProvider;
    @Bean
    public AiChatService aiChatService() {
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();
        AiChatService aiChatService = AiServices.builder(AiChatService.class)
                .chatModel(qwenChatModel)
                .streamingChatModel(qwenStreamingChatModel)
                .chatMemory(chatMemory)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
                .contentRetriever(contentRetriever)
                .toolProvider(mcpToolProvider)
                .build();
        return aiChatService;
    }
}
