package com.syuro.wibusystem.security.session.service;

import com.syuro.wibusystem.security.session.api.SessionCachePayload;
import com.syuro.wibusystem.security.session.config.SessionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class SessionTokenService {

    private final SessionProperties props;
    private final ObjectMapper objectMapper;

    // ─── Session Token (opaque) ────────────────────────────────────────────────

    /**
     * Sinh raw token ngẫu nhiên 32 bytes — sẽ được SHA-256 hash trước khi lưu DB.
     */
    public String generateRawToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Ký session_token để gửi FE.
     * Format: {rawToken}.{HMAC(primarySecret, rawToken)}
     */
    public String signToken(String rawToken) {
        String sig = hmac(props.primarySecret(), rawToken);
        return rawToken + "." + sig;
    }

    /**
     * Verify chữ ký, trả rawToken nếu hợp lệ; thử tất cả secrets để hỗ trợ key rotation.
     */
    public String verifyToken(String signedToken) {
        if (signedToken == null) return null;
        int dot = signedToken.lastIndexOf('.');
        if (dot < 0) return null;

        String rawToken = signedToken.substring(0, dot);
        String sig = signedToken.substring(dot + 1);

        for (String secret : props.secrets()) {
            if (secret == null || secret.isBlank()) continue;
            if (constantTimeEquals(hmac(secret, rawToken), sig)) {
                return rawToken;
            }
        }
        return null;
    }

    // ─── Session Data Cache ────────────────────────────────────────────────────

    /**
     * Ký session_data payload thành cookie string.
     * Format: {base64url(json)}.{expiresAtMillis}.{HMAC(secret, encoded.expiresAt)}
     */
    public String signSessionData(SessionCachePayload payload) {
        try {
            long expiresAt = Instant.now().plusSeconds(props.cacheTtl()).toEpochMilli();
            payload = payload.withExpiresAt(expiresAt);

            String json = objectMapper.writeValueAsString(payload);
            String encoded = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(json.getBytes(StandardCharsets.UTF_8));
            String sig = hmac(props.primarySecret(), encoded + "." + expiresAt);

            return encoded + "." + expiresAt + "." + sig;
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Verify và parse session_data cookie. Trả null nếu chữ ký sai hoặc đã hết hạn.
     */
    public SessionCachePayload verifySessionData(String signed) {
        if (signed == null) return null;
        String[] parts = signed.split("\\.");
        if (parts.length != 3) return null;

        String encoded = parts[0];
        String expiresAtStr = parts[1];
        String sig = parts[2];

        long expiresAt;
        try {
            expiresAt = Long.parseLong(expiresAtStr);
        } catch (NumberFormatException e) {
            return null;
        }

        if (Instant.now().toEpochMilli() > expiresAt) return null;

        boolean valid = false;
        for (String secret : props.secrets()) {
            if (secret == null || secret.isBlank()) continue;
            if (constantTimeEquals(hmac(secret, encoded + "." + expiresAtStr), sig)) {
                valid = true;
                break;
            }
        }
        if (!valid) return null;

        try {
            String json = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
            return objectMapper.readValue(json, SessionCachePayload.class);
        } catch (Exception e) {
            return null;
        }
    }

    // ─── Utilities ─────────────────────────────────────────────────────────────

    private String hmac(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Constant-time comparison để chống timing attack
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        byte[] ab = a.getBytes(StandardCharsets.UTF_8);
        byte[] bb = b.getBytes(StandardCharsets.UTF_8);
        if (ab.length != bb.length) return false;
        int diff = 0;
        for (int i = 0; i < ab.length; i++) diff |= ab[i] ^ bb[i];
        return diff == 0;
    }
}
