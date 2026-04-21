package com.syuro.wibusystem.security.session.anonymous.service;

import com.syuro.wibusystem.security.session.anonymous.AnonymousSessionProperties;
import com.syuro.wibusystem.security.session.anonymous.dto.AnonymousSessionResponse;
import com.syuro.wibusystem.security.session.anonymous.dto.DeviceInfo;
import com.syuro.wibusystem.shared.exception.AppException;
import com.syuro.wibusystem.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

/**
 * Quản lý vòng đời của anonymous session:
 * - Tạo mới hoặc trả về session cũ nếu cùng fingerprint thiết bị (deduplication)
 * - Rate limit theo IP để chống spam tạo session
 * - TTL tự động dọn rác — không cần cron job
 * <p>
 * Key Redis (hash tag tương thích cluster):
 * {anon:session}:{uuid}     Hash  — dữ liệu session, TTL = app.anonymous-session.ttl
 * {anon:fp}:{sha256}        String — uuid, dùng để dedup theo fingerprint, TTL đồng bộ
 * ratelimit:anon:{ip}       String — counter, TTL 1 giờ
 */
@Service
@RequiredArgsConstructor
public class AnonymousSessionService {

    private static final String SESSION_PREFIX = "{anon:session}:";
    private static final String FP_PREFIX = "{anon:fp}:";
    private static final String RATE_LIMIT_PREFIX = "ratelimit:anon:";

    @Qualifier("sessionRedisTemplate")
    private final RedisTemplate<String, Object> redis;
    private final AnonymousSessionProperties props;

    /**
     * Tạo anonymous session mới hoặc trả về session hiện có nếu cùng thiết bị.
     *
     * @param deviceInfo thông tin thiết bị từ client
     * @param clientIp   IP để rate limit (đã được extract bởi controller)
     */
    public AnonymousSessionResponse createOrRenew(DeviceInfo deviceInfo, String clientIp) {
        String fpHash = fingerprint(deviceInfo);

        // 1. Kiểm tra dedup — cùng fingerprint và session vẫn còn sống
        String existingToken = (String) redis.opsForValue().get(FP_PREFIX + fpHash);
        if (existingToken != null && Boolean.TRUE.equals(redis.hasKey(SESSION_PREFIX + existingToken))) {
            renewTtl(existingToken, fpHash);
            return new AnonymousSessionResponse(existingToken);
        }

        // 2. Rate limit — tối đa N session mới / IP / giờ
        enforceRateLimit(clientIp);

        // 3. Tạo session mới
        String token = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Duration ttl = props.ttl();

        redis.opsForHash().putAll(SESSION_PREFIX + token, Map.of(
                "fingerprint", fpHash,
                "userAgent", nullSafe(deviceInfo.userAgent()),
                "createdAt", now.toString(),
                "lastActivityAt", now.toString(),
                "activityCount", "0"
        ));
        redis.expire(SESSION_PREFIX + token, ttl);
        redis.opsForValue().set(FP_PREFIX + fpHash, token, ttl);

        return new AnonymousSessionResponse(token);
    }

    /**
     * Kiểm tra session token có tồn tại và còn sống không.
     */
    public boolean exists(String token) {
        return Boolean.TRUE.equals(redis.hasKey(SESSION_PREFIX + token));
    }

    /**
     * Tăng activityCount của session.
     * Khi đạt ngưỡng app.anonymous-session.activity-threshold thì caller có thể
     * quyết định promote session lên DB (tạo User với isAnonymous=true).
     *
     * @return activityCount sau khi tăng
     */
    public long incrementActivity(String token) {
        String key = SESSION_PREFIX + token;
        Long count = redis.opsForHash().increment(key, "activityCount", 1);
        // Cập nhật lastActivityAt và reset TTL mỗi khi có activity
        redis.opsForHash().put(key, "lastActivityAt", Instant.now().toString());
        redis.expire(key, props.ttl());
        return count != null ? count : 0;
    }

    /**
     * Xóa session sau khi đã merge vào user thật (claim).
     */
    public void invalidate(String token) {
        Object fpHash = redis.opsForHash().get(SESSION_PREFIX + token, "fingerprint");
        redis.delete(SESSION_PREFIX + token);
        if (fpHash instanceof String fp) {
            redis.delete(FP_PREFIX + fp);
        }
    }

    // --- private helpers ---

    private void renewTtl(String token, String fpHash) {
        Duration ttl = props.ttl();
        redis.expire(SESSION_PREFIX + token, ttl);
        redis.expire(FP_PREFIX + fpHash, ttl);
    }

    private void enforceRateLimit(String clientIp) {
        String key = RATE_LIMIT_PREFIX + clientIp;
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redis.expire(key, Duration.ofHours(1));
        }
        if (count != null && count > props.rateLimitPerHour()) {
            throw new AppException(ErrorCode.RATE_LIMIT_EXCEEDED);
        }
    }

    /**
     * SHA-256 của tổ hợp device attributes — dùng làm dedup key.
     * Không phải fingerprint hoàn hảo nhưng đủ để giảm đáng kể session rác từ cùng thiết bị.
     */
    private String fingerprint(DeviceInfo info) {
        String raw = String.join("|",
                nullSafe(info.userAgent()),
                nullSafe(info.language()),
                nullSafe(info.timezone()),
                nullSafe(info.screenResolution()),
                nullSafe(info.platform())
        );
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 được đảm bảo tồn tại trong mọi JVM theo spec
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private String nullSafe(String value) {
        return value != null ? value.trim() : "";
    }
}
