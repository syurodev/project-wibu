package com.syuro.wibusystem.security.auth.service;

import com.syuro.wibusystem.security.auth.dto.PendingRegistration;
import com.syuro.wibusystem.shared.exception.AppException;
import com.syuro.wibusystem.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final String KEY_PREFIX = "otp:registration:";
    private static final Duration TTL = Duration.ofMinutes(15);

    private final RedisTemplate<String, Object> sessionRedisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateAndStore(String userId, String email, String name, String passwordHash, String language) {
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        sessionRedisTemplate.opsForValue().set(
                KEY_PREFIX + userId,
                new PendingRegistration(otp, email, name, passwordHash, language),
                TTL
        );
        return otp;
    }

    public PendingRegistration verifyAndConsume(String userId, String inputOtp) {
        String key = KEY_PREFIX + userId;
        Object raw = sessionRedisTemplate.opsForValue().get(key);
        if (raw == null) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }
        PendingRegistration pending = (PendingRegistration) raw;
        if (!pending.otp().equals(inputOtp)) {
            throw new AppException(ErrorCode.OTP_INVALID);
        }
        sessionRedisTemplate.delete(key);
        return pending;
    }
}
