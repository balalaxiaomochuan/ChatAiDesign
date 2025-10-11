package com.huawei.chataidesign.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // 设置 Key 的序列化器为 StringRedisSerializer
        template.setKeySerializer(new StringRedisSerializer());
        // 设置 Value 的序列化器为 GenericJackson2JsonRedisSerializer，将对象序列化为 JSON
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        // 设置 Hash 结构中的 Key 和 Value 的序列化器
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }
}
