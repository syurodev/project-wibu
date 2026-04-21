package com.syuro.wibusystem.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, Object> sessionRedisTemplate;

    private static final String PREFIX = "blacklist:sid:";

    public void blacklist(Long sessionId, Duration ttl) {
        sessionRedisTemplate.opsForValue().set(PREFIX + sessionId, "1", ttl);
    }

    public boolean isBlacklisted(Long sessionId) {
        return Boolean.TRUE.equals(sessionRedisTemplate.hasKey(PREFIX + sessionId));
    }
}
