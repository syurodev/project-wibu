package com.syuro.wibusystem.security.passkey.service;

import com.syuro.wibusystem.security.passkey.config.PasskeyProperties;
import com.syuro.wibusystem.shared.exception.AppException;
import com.syuro.wibusystem.shared.exception.ErrorCode;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(PasskeyProperties.class)
public class PasskeyChallengeService {

    private static final String REG_KEY  = "passkey:reg:challenge:";
    private static final String AUTH_KEY = "passkey:auth:challenge:";

    private final RedisTemplate<String, Object> sessionRedisTemplate;
    private final PasskeyProperties passkeyProperties;

    public void storeRegistration(Long userId, PublicKeyCredentialCreationOptions options) {
        try {
            sessionRedisTemplate.opsForValue().set(
                    REG_KEY + userId,
                    options.toJson(),
                    Duration.ofSeconds(passkeyProperties.challengeTtlSeconds())
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize registration options", e);
        }
    }

    public PublicKeyCredentialCreationOptions loadAndDeleteRegistration(Long userId) {
        Object raw = sessionRedisTemplate.opsForValue().getAndDelete(REG_KEY + userId);
        if (raw == null) throw new AppException(ErrorCode.PASSKEY_CHALLENGE_EXPIRED);
        try {
            return PublicKeyCredentialCreationOptions.fromJson((String) raw);
        } catch (Exception e) {
            throw new AppException(ErrorCode.PASSKEY_CHALLENGE_EXPIRED);
        }
    }

    public String storeAuthentication(AssertionRequest request) {
        String sessionKey = UUID.randomUUID().toString();
        try {
            sessionRedisTemplate.opsForValue().set(
                    AUTH_KEY + sessionKey,
                    request.toJson(),
                    Duration.ofSeconds(passkeyProperties.challengeTtlSeconds())
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize assertion request", e);
        }
        return sessionKey;
    }

    public AssertionRequest loadAndDeleteAuthentication(String sessionKey) {
        Object raw = sessionRedisTemplate.opsForValue().getAndDelete(AUTH_KEY + sessionKey);
        if (raw == null) throw new AppException(ErrorCode.PASSKEY_CHALLENGE_EXPIRED);
        try {
            return AssertionRequest.fromJson((String) raw);
        } catch (Exception e) {
            throw new AppException(ErrorCode.PASSKEY_CHALLENGE_EXPIRED);
        }
    }
}
