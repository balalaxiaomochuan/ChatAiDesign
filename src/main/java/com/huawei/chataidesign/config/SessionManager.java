package com.huawei.chataidesign.config;

import com.huawei.chataidesign.entity.User;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class SessionManager {

    private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();
    private final Random random = new SecureRandom();

    /**
     * 创建会话
     */
    public String createSession(User user) {
        String sessionId = generateSessionId();
        SessionInfo sessionInfo = new SessionInfo(user, System.currentTimeMillis() + 24 * 60 * 60 * 1000);
        sessions.put(sessionId, sessionInfo);
        return sessionId;
    }

    /**
     * 根据sessionId获取用户
     */
    public User getUserBySessionId(String sessionId) {
        SessionInfo sessionInfo = sessions.get(sessionId);
        if (sessionInfo == null || sessionInfo.getExpireTime() < System.currentTimeMillis()) {
            return null;
        }
        return sessionInfo.getUser();
    }

    /**
     * 生成会话ID
     */
    private String generateSessionId() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 会话信息类
     */
    private static class SessionInfo {
        private User user;
        private long expireTime;

        public SessionInfo(User user, long expireTime) {
            this.user = user;
            this.expireTime = expireTime;
        }

        // getters
        public User getUser() { return user; }
        public long getExpireTime() { return expireTime; }
    }
}
