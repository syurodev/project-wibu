package com.syuro.wibusystem.security.session.filter;

import com.syuro.wibusystem.security.session.dto.SessionCachePayload;
import com.syuro.wibusystem.security.session.repository.SessionRepository;
import com.syuro.wibusystem.security.session.service.SessionExtendService;
import com.syuro.wibusystem.security.session.service.SessionTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

@Component
@Order(10)
@RequiredArgsConstructor
public class SessionAuthFilter extends OncePerRequestFilter {

    private final SessionTokenService tokenService;
    private final SessionRepository sessionRepository;
    private final SessionExtendService sessionExtendService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        // 1. Đọc X-Session-Token header
        String signedToken = request.getHeader("X-Session-Token");
        if (signedToken == null) {
            chain.doFilter(request, response);
            return;
        }

        // 2. Verify chữ ký → lấy rawToken
        String rawToken = tokenService.verifyToken(signedToken);
        if (rawToken == null) {
            chain.doFilter(request, response);
            return;
        }

        // 3. Kiểm tra session_data cache (không cần DB)
        String sessionDataHeader = request.getHeader("X-Session-Cache");
        SessionCachePayload cached = tokenService.verifySessionData(sessionDataHeader);

        if (cached != null) {
            setAuthentication(cached, request);

            // Nếu cache gần hết hạn (< 60 giây) → trả cache mới trong response
            long remaining = cached.expiresAt() - Instant.now().toEpochMilli();
            if (remaining < 60_000) {
                response.setHeader("X-New-Session-Cache", tokenService.signSessionData(cached));
            }

            chain.doFilter(request, response);
            return;
        }

        // 4. Cache MISS → DB lookup
        String tokenHash = sha256(rawToken);
        var session = sessionRepository.findByRefreshTokenHash(tokenHash).orElse(null);

        if (session == null || session.getRevokedAt() != null
                || session.getExpiresAt().isBefore(Instant.now())) {
            chain.doFilter(request, response);
            return;
        }

        // 5. Sliding window
        sessionExtendService.extendIfNeeded(session);

        // 6. Build payload + set SecurityContext
        SessionCachePayload payload = sessionExtendService.buildPayload(session);
        setAuthentication(payload, request);

        // 7. Trả session_data mới trong response header → FE cập nhật cookie
        response.setHeader("X-New-Session-Cache", tokenService.signSessionData(payload));

        chain.doFilter(request, response);
    }

    private void setAuthentication(SessionCachePayload payload, HttpServletRequest request) {
        List<SimpleGrantedAuthority> authorities = payload.permissions().stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        var auth = new UsernamePasswordAuthenticationToken(
                payload.userId(), payload, authorities);
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private String sha256(String input) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
