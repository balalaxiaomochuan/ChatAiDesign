package com.huawei.chataidesign.service;

import com.huawei.chataidesign.entity.User;
import com.huawei.chataidesign.mapper.UserMapper;
import com.huawei.chataidesign.repository.redis.RedisRepository;
import com.huawei.chataidesign.utils.JacksonUtil;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;


@Component
public class CustomUserDetailsService implements UserDetailsService {
    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisRepository redisRepository;

    public User getUserByName(String username) {
        Object userinfo = redisRepository.get(username);
        if (userinfo == null) {
            User user = userMapper.getUserByUserName(username);
            redisRepository.setEx(username, user, 60 * 60 * 24);
            return user;
        } else {
            return JacksonUtil.toObject(userinfo.toString(), User.class);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        Object userinfo = redisRepository.get(username);
        User user = null;
        if (userinfo == null) {
            user = userMapper.getUserByUserName(username);
            redisRepository.setEx(username, JacksonUtil.toJsonString(user), 60 * 60 * 24);
        } else {
            user = JacksonUtil.toObject(userinfo.toString(), User.class);
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .build();
    }
//    private Collection<? extends GrantedAuthority> getAuthorities(com.huawei.chataidesign.entity.User user) {
//        return user.getRoles().stream()
//                .map(role -> new SimpleGrantedAuthority(role.getName()))
//                .collect(Collectors.toList());
//    }
}