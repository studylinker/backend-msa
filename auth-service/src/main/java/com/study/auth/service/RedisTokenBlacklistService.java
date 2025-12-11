package com.study.auth.service;

import com.study.common.security.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisTokenBlacklistService implements TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean isBlacklisted(String token) {
        // Redis에 해당 토큰 키가 존재하면 블랙리스트임
        return Boolean.TRUE.equals(redisTemplate.hasKey(token));
    }
}
