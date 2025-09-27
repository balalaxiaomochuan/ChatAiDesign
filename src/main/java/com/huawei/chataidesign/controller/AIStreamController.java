package com.huawei.chataidesign.controller;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

@RestController
@RequestMapping("/api/ai")
@Slf4j
public class AIStreamController {
    private static final String PRESET_PROMPT = "请不要生成MarkDown形式。\n" +
            "请在回答完之后，提供几个用户还有可能会想问的问题（两到三个）。";

    /**
     * 流式调用大模型API接口
     *
     * @param prompt 用户输入的提示词
     * @return 流式响应结果
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> streamAIResponse(@RequestBody String prompt) {
        log.info("Received AI stream request with prompt: {}", prompt);
        String finalPrompt = PRESET_PROMPT + prompt;
        StreamingResponseBody responseBody = outputStream -> {
            CountDownLatch latch = new CountDownLatch(1);

            try {
                // 调用通义千问API并流式返回结果
                callQwenAPIWithSDK(finalPrompt, outputStream, latch);
                latch.await(); // 等待异步任务完成
            } catch (Exception e) {
                log.error("Error occurred while streaming AI response", e);
                try {
                    // 将错误信息写入输出流，保持SSE格式
                    outputStream.write(("data: Error: " + e.getMessage() + "\n\n").getBytes());
                    outputStream.flush();
                } catch (Exception ioException) {
                    log.error("Error writing to output stream", ioException);
                } finally {
                    latch.countDown();
                }
            } finally {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    log.error("Error closing output stream", e);
                }
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(responseBody);
    }

    /**
     * 使用DashScope SDK调用通义千问API并流式返回结果
     *
     * @param prompt 用户输入
     * @param outputStream 响应输出流
     * @param latch 计数锁存器
     * @throws Exception 异常
     */
    private void callQwenAPIWithSDK(String prompt, java.io.OutputStream outputStream, CountDownLatch latch) throws Exception {
        // 1. 获取 API Key
        String apiKey = System.getenv("ALI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("请设置环境变量 ALI_API_KEY");
        }

        // 2. 初始化 Generation 实例
        Generation gen = new Generation();

        // 3. 构建请求参数
        GenerationParam param = GenerationParam.builder()
                .apiKey(apiKey)
                .model("qwen-plus")
                .messages(Arrays.asList(
                        Message.builder()
                                .role(Role.USER.getValue())
                                .content(prompt)
                                .build()
                ))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .incrementalOutput(true) // 开启增量输出，流式返回
                .build();

        // 4. 发起流式调用并处理响应
        Flowable<GenerationResult> result = gen.streamCall(param);

        result
                .subscribeOn(Schedulers.io()) // IO线程执行请求
                .observeOn(Schedulers.computation()) // 计算线程处理响应
                .subscribe(
                        // onNext: 处理每个响应片段
                        message -> {
                            try {
                                String content = message.getOutput().getChoices().get(0).getMessage().getContent();
                                String finishReason = message.getOutput().getChoices().get(0).getFinishReason();

                                // 将换行符替换为自定义占位符（类似Python代码的处理方式）
                                String processedContent = content.replace("\n", "<|newline|>");

                                // 输出内容，保持正确的SSE格式
                                outputStream.write(("data: " + processedContent + "\n\n").getBytes());
                                outputStream.flush();
                            } catch (Exception e) {
                                log.error("Error processing message", e);
                            }
                        },
                        // onError: 处理错误
                        error -> {
                            try {
                                String errorMessage = error.getMessage()
                                        .replace("\n", "\\n")
                                        .replace("\r", "\\r");
                                outputStream.write(("data: Error: " + errorMessage + "\n\n").getBytes());
                                outputStream.flush();
                            } catch (Exception e) {
                                log.error("Error writing error to output stream", e);
                            } finally {
                                latch.countDown();
                            }
                        },
                        // onComplete: 完成回调
                        () -> {
                            try {
                                outputStream.write("data: [DONE]\n\n".getBytes());
                                outputStream.flush();
                            } catch (Exception e) {
                                log.error("Error writing completion to output stream", e);
                            } finally {
                                latch.countDown();
                            }
                        }
                );
    }

}
