package com.huawei.chataidesign.controller;

import com.huawei.chataidesign.entity.request.PromptReq;
import com.huawei.chataidesign.service.AiChatService;
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

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamAIResponseWithChatService(@RequestBody PromptReq promptReq) {
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
}
