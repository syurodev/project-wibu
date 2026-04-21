package com.syuro.wibusystem.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filter chạy một lần mỗi request, đứng trước UsernamePasswordAuthenticationFilter.
 *
 * Luồng xử lý:
 *   1. Đọc header "Authorization: Bearer <token>"
 *   2. Parse và validate JWT
 *   3. Trích xuất userId + authorities từ claims
 *   4. Set Authentication vào SecurityContext để các filter/controller sau nhận ra user
 *
 * Nếu token không có hoặc không hợp lệ → bỏ qua, không set authentication.
 * Request sẽ bị từ chối bởi các rule trong SecurityConfig nếu endpoint yêu cầu xác thực.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        // Không có Bearer token → tiếp tục chain, không làm gì thêm
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7); // bỏ prefix "Bearer "

        try {
            Claims claims = jwtService.parseToken(token);
            Long sessionId = jwtService.extractSessionId(claims);

            if (tokenBlacklistService.isBlacklisted(sessionId)) {
                chain.doFilter(request, response);
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                Long userId = jwtService.extractUserId(claims);
                List<SimpleGrantedAuthority> authorities = jwtService.extractAuthorities(claims)
                        .stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                // principal   = userId (Long — Snowflake ID)
                // credentials = sessionId (Long — Snowflake ID) — dùng để xác định session hiện tại,
                //               ví dụ: đánh dấu "thiết bị này" trong danh sách session.
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userId, sessionId, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (JwtException | IllegalArgumentException ignored) {
            // Token không hợp lệ hoặc đã hết hạn — không set authentication
            // Request sẽ bị reject bởi SecurityConfig nếu endpoint cần xác thực
        }

        chain.doFilter(request, response);
    }
}
