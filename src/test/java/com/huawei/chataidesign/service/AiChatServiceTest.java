package com.huawei.chataidesign.service;


import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiChatServiceTest {

    @Resource
    private AiChatService aiCodeHelper;

    @Test
    void chat() {
        String result = aiCodeHelper.chat("你好，我是xiezichuan");
        System.out.println(result);
    }

//    @Test
//    void chatWithMultiModal() {
//        UserMessage userMessage = UserMessage.from(
//                TextContent.from("描述图片"),
//                ImageContent.from("https://mmbiz.qpic.cn/mmbiz_png/mngWTkJEOYK5rVrxmRUf1ibzQR638JNlQ3jcmaDImKh4WuCrgW9zMVhF9JfXB0a8QQxicr07ibnyNdqQOgMxNpl3A/640?wx_fmt=png&from=appmsg&tp=webp&wxfrom=5&wx_lazy=1#imgIndex=14")
//        );
//        aiCodeHelper.chatWithMessage(userMessage);
//    }

    @Test
    void chatWithMemery() {
        String result = aiCodeHelper.chat("你好，我是xiezichuan");
        System.out.println(result);
        result = aiCodeHelper.chat("我是谁？");
        System.out.println(result);
    }

    @Test
    void chatWithRag() {
        String result = aiCodeHelper.chat("请帮我列举一些计算机面试题，要求语言精简");
        System.out.println(result);
    }

    @Test
    void chatWithMcp() {
        String result = aiCodeHelper.chat("什么是小林Coding?");
        System.out.println(result);
    }
}
