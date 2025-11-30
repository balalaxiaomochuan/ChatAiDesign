package com.huawei.chataidesign.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

/**
 * AI聊天服务接口
 * 使用LangChain4j的AiServices动态代理实现
 */
public interface AiChatService {

//    @SystemMessage(fromResource = "system-prompt.txt")
//    String chat(String message);

    @SystemMessage(fromResource = "system-prompt.txt")
    String chat(String message);


    @SystemMessage(fromResource = "system-prompt.txt")
    Flux<String> chatWithStream(@MemoryId int memoryId, @UserMessage String message);
}
