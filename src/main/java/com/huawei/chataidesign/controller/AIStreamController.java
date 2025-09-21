package com.huawei.chataidesign.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
@Slf4j
public class AIStreamController {

    /**
     * 流式调用大模型API接口
     * 
     * @param prompt 用户输入的提示词
     * @return 流式响应结果
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> streamAIResponse(@RequestBody String prompt) {
        log.info("Received AI stream request with prompt: {}", prompt);

        StreamingResponseBody responseBody = outputStream -> {
            try {
                // 调用真实的通义千问API并流式返回结果
                callRealQwenAPI(prompt, outputStream);
            } catch (Exception e) {
                log.error("Error occurred while streaming AI response", e);
                // 将错误信息写入输出流，保持SSE格式
                outputStream.write(("data: Error: " + e.getMessage() + "\n\n").getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            } finally {
                outputStream.close();
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(responseBody);
    }

    /**
     * 调用真实的通义千问API并流式返回结果
     * 
     * @param prompt 用户输入
     * @param outputStream 响应输出流
     * @throws IOException IO异常
     */
    private void callRealQwenAPI(String prompt, OutputStream outputStream) throws IOException {
        // 第一步：生成访问令牌
        String accessToken = generateAccessToken();
        
        // 第二步：建立SSE连接
        String sseUrl = "https://bailian-stream-api.aliyuncs.com/sse/console4Json/" + accessToken;
        
        // 第三步：发送消息并获取流式响应
        sendMessageAndStreamResponse(prompt, sseUrl, outputStream);
    }

    /**
     * 生成访问令牌
     * 
     * @return 访问令牌
     * @throws IOException IO异常
     */
    private String generateAccessToken() throws IOException {
        String apiUrl = "https://bailian-cs.console.aliyun.com/data/api.json?action=BroadScopeAspnGateway&product=sfm_bailian&api=zeldaEasy.cornerstoneStreamGateway.streamGatewayConsoleService.generateAccessToken&_v=undefined";
        
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("region", "cn-beijing");
        connection.setRequestProperty("sec_token", "Nx38Yn8QwqJF4t3Hhzlri1");
        connection.setDoOutput(true);
        
        // 构造请求参数
        String feTraceId = UUID.randomUUID().toString();
        String requestBody = "{\n" +
                "  \"params\": \"{\\\"Api\\\":\\\"zeldaEasy.cornerstoneStreamGateway.streamGatewayConsoleService.generateAccessToken\\\",\\\"V\\\":\\\"1.0\\\",\\\"Data\\\":{\\\"accessTokenRequest\\\":{\\\"source\\\":\\\"\\\"},\\\"cornerstoneParam\\\":{\\\"feTraceId\\\":\\\"" + feTraceId + "\\\",\\\"feURL\\\":\\\"https://bailian.console.aliyun.com/?spm=5176.30260724.J_VtPmWTA0QjMbFsMNZMB1P.1.548b7de1rt2v2u&tab=model#/efm/model_experience_center/text?currentTab=textChat&modelId=qwen3-next-80b-a3b-instruct\\\",\\\"protocol\\\":\\\"V2\\\",\\\"console\\\":\\\"ONE_CONSOLE\\\",\\\"productCode\\\":\\\"p_efm\\\",\\\"switchAgent\\\":12814336,\\\"switchUserType\\\":3,\\\"domain\\\":\\\"bailian.console.aliyun.com\\\",\\\"userNickName\\\":\\\"\\\",\\\"userPrincipalName\\\":\\\"\\\",\\\"xsp_lang\\\":\\\"zh-CN\\\"}}}\",\n" +
                "  \"region\": \"cn-beijing\",\n" +
                "  \"sec_token\": \"Nx38Yn8QwqJF4t3Hhzlri1\"\n" +
                "}";
        
        // 发送请求
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        // 读取响应
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                
                // 解析访问令牌
                String responseStr = response.toString();
                int tokenStart = responseStr.indexOf("\"accessToken\":\"") + 15;
                int tokenEnd = responseStr.indexOf("\"", tokenStart);
                return responseStr.substring(tokenStart, tokenEnd);
            }
        } else {
            throw new IOException("Failed to generate access token, response code: " + responseCode);
        }
    }

    /**
     * 发送消息并获取流式响应
     * 
     * @param prompt 用户输入
     * @param sseUrl SSE连接地址
     * @param outputStream 响应输出流
     * @throws IOException IO异常
     */
    private void sendMessageAndStreamResponse(String prompt, String sseUrl, OutputStream outputStream) throws IOException {
        // 先发送消息
        String sendMessageUrl = "https://bailian-cs.console.aliyun.com/data/api.json?action=BroadScopeAspnGateway&product=sfm_bailian&api=zeldaEasy.broadscope-platform.messageConsoleApiService.getRemind&_v=undefined";
        
        URL url = new URL(sendMessageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("region", "cn-beijing");
        connection.setRequestProperty("sec_token", "Nx38Yn8QwqJF4t3Hhzlri1");
        connection.setDoOutput(true);
        
        // 构造请求参数
        String feTraceId = UUID.randomUUID().toString();
        String requestBody = "{\n" +
                "  \"params\": \"{\\\"Api\\\":\\\"zeldaEasy.broadscope-platform.messageConsoleApiService.getRemind\\\",\\\"V\\\":\\\"1.0\\\",\\\"Data\\\":{\\\"cornerstoneParam\\\":{\\\"feTraceId\\\":\\\"" + feTraceId + "\\\",\\\"feURL\\\":\\\"https://bailian.console.aliyun.com/?spm=5176.30260724.J_VtPmWTA0QjMbFsMNZMB1P.1.548b7de1rt2v2u&tab=model#/efm/model_experience_center/text?currentTab=textChat&modelId=qwen3-next-80b-a3b-instruct\\\",\\\"protocol\\\":\\\"V2\\\",\\\"console\\\":\\\"ONE_CONSOLE\\\",\\\"productCode\\\":\\\"p_efm\\\",\\\"switchAgent\\\":12814336,\\\"switchUserType\\\":3,\\\"domain\\\":\\\"bailian.console.aliyun.com\\\",\\\"userNickName\\\":\\\"\\\",\\\"userPrincipalName\\\":\\\"\\\",\\\"xsp_lang\\\":\\\"zh-CN\\\"}}}\",\n" +
                "  \"region\": \"cn-beijing\",\n" +
                "  \"sec_token\": \"Nx38Yn8QwqJF4t3Hhzlri1\"\n" +
                "}";
        
        // 发送请求
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        // 建立SSE连接并读取流式响应
        URL sseURL = new URL(sseUrl);
        HttpURLConnection sseConnection = (HttpURLConnection) sseURL.openConnection();
        sseConnection.setRequestMethod("GET");
        sseConnection.setRequestProperty("Accept", "text/event-stream");
        sseConnection.setRequestProperty("Connection", "keep-alive");
        sseConnection.setRequestProperty("Cache-Control", "no-cache");
        
        // 读取流式响应
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(sseConnection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                // 将响应数据写入输出流
                outputStream.write((line + "\n").getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            }
        }
        
        connection.disconnect();
        sseConnection.disconnect();
    }
}