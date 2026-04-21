package com.syuro.wibusystem.security.auth.service;

import com.syuro.wibusystem.security.auth.dto.MagicLinkPending;
import com.syuro.wibusystem.shared.exception.AppException;
import com.syuro.wibusystem.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class MagicLinkTokenService {

    private static final String TOKEN_KEY    = "magic-link:login:";
    private static final String USER_KEY     = "magic-link:user:";
    private static final String RATE_KEY     = "magic-link:rate:";
    private static final Duration TTL        = Duration.ofMinutes(15);
    private static final int MAX_PER_WINDOW  = 3;

    private final RedisTemplate<String, Object> sessionRedisTemplate;

    public void store(String token, Long userId, String email) {
        enforceRateLimit(userId);

        String userKey = USER_KEY + userId;

        // Xóa token cũ nếu còn tồn tại — đảm bảo chỉ có 1 token active per user
        Object oldToken = sessionRedisTemplate.opsForValue().getAndDelete(userKey);
        if (oldToken instanceof String old) {
            sessionRedisTemplate.delete(TOKEN_KEY + old);
        }

        sessionRedisTemplate.opsForValue().set(TOKEN_KEY + token, new MagicLinkPending(userId, email), TTL);
        sessionRedisTemplate.opsForValue().set(userKey, token, TTL);
    }

    private void enforceRateLimit(Long userId) {
        String rateKey = RATE_KEY + userId;
        Long count = sessionRedisTemplate.opsForValue().increment(rateKey);
        if (count == 1) {
            // Chỉ set TTL lần đầu để window tự reset sau 15 phút
            sessionRedisTemplate.expire(rateKey, TTL);
        }
        if (count > MAX_PER_WINDOW) {
            throw new AppException(ErrorCode.RATE_LIMIT_EXCEEDED);
        }
    }

    public MagicLinkPending verifyAndConsume(String token) {
        String tokenKey = TOKEN_KEY + token;
        Object raw = sessionRedisTemplate.opsForValue().get(tokenKey);
        if (raw == null) {
            throw new AppException(ErrorCode.MAGIC_LINK_INVALID);
        }
        MagicLinkPending pending = (MagicLinkPending) raw;
        // Xóa cả token key lẫn user index
        sessionRedisTemplate.delete(tokenKey);
        sessionRedisTemplate.delete(USER_KEY + pending.userId());
        return pending;
    }
}
