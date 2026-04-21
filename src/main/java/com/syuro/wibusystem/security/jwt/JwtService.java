package com.syuro.wibusystem.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Service xử lý toàn bộ logic liên quan đến JWT:
 *   - Tạo access token từ các giá trị thô (không phụ thuộc UserPrincipal)
 *   - Parse và validate token
 *   - Trích xuất thông tin (userId, sessionId, authorities) từ claims
 *
 * JwtService được giữ độc lập hoàn toàn với module users để tránh circular dependency
 * và đảm bảo có thể tái sử dụng (ví dụ: OAuth2, service-to-service token).
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    /**
     * Tạo SecretKey từ chuỗi Base64 trong config.
     * Dùng HMAC-SHA (tự chọn độ dài key phù hợp: HS256/HS384/HS512).
     */
    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secret()));
    }

    /**
     * Tạo JWT access token chứa:
     *   - sub        : Snowflake ID của user (dạng String)
     *   - sid        : Snowflake ID của RefreshToken (session ID) — dùng để xác định "thiết bị hiện tại"
     *                  khi user xem danh sách session, và là điểm gắn Redis blacklist sau này.
     *   - email      : email đăng nhập
     *   - username   : tên hiển thị (có thể null)
     *   - authorities: danh sách quyền đã expand wildcard (chuỗi, ví dụ "content:create")
     *   - iat / exp  : thời điểm phát hành và hết hạn
     *
     * Nhận giá trị thô thay vì UserPrincipal để JwtService không phụ thuộc vào module users.
     * Caller (AuthService) chịu trách nhiệm trích xuất dữ liệu từ UserPrincipal trước khi gọi.
     *
     * TODO[Redis Blacklist]: Khi cần revoke access token ngay lập tức (đổi mật khẩu, admin ban, remote logout),
     *   lưu "sid" vào Redis với TTL = thời gian còn lại của access token.
     *   Trong JwtAuthFilter, sau khi parse token thành công, kiểm tra Redis xem "sid" có bị blacklist không.
     *   Key gợi ý: "blacklist:session:{sid}" hoặc "blacklist:token:{jti}" nếu thêm claim jti.
     *
     * @param userId      Snowflake ID của user (sẽ thành claim "sub")
     * @param sessionId   Snowflake ID của RefreshToken hiện tại (claim "sid")
     * @param email       Email đăng nhập (claim "email")
     * @param username    Tên hiển thị, có thể null (claim "username")
     * @param authorities Danh sách quyền dưới dạng chuỗi (claim "authorities")
     */
    public String generateAccessToken(Long userId, Long sessionId, String email,
                                      String username, Collection<String> authorities) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("sid", sessionId.toString())   // session ID để trace + blacklist
                .claim("email", email)
                .claim("username", username)
                .claim("authorities", List.copyOf(authorities))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(jwtProperties.accessTokenExpiry())))
                .signWith(signingKey())
                .compact();
    }

    /** Lấy Snowflake ID của session từ claim "sid". */
    public Long extractSessionId(Claims claims) {
        return Long.parseLong((String) claims.get("sid"));
    }

    /**
     * Parse và xác minh chữ ký JWT.
     * Ném JwtException nếu token không hợp lệ hoặc đã hết hạn.
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Kiểm tra token có hợp lệ không (chữ ký đúng + chưa hết hạn).
     */
    public boolean isValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /** Lấy Snowflake ID của user từ claim "sub". */
    public Long extractUserId(Claims claims) {
        return Long.parseLong(claims.getSubject());
    }

    /** Lấy danh sách quyền từ claim "authorities". */
    @SuppressWarnings("unchecked")
    public List<String> extractAuthorities(Claims claims) {
        return (List<String>) claims.get("authorities");
    }
}
