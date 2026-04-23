package com.syuro.wibusystem.security.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@Component
@Order(1)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, Object> sessionRedisTemplate;

    private record RateRule(int maxRequests, Duration window) {}

    private static final Map<String, RateRule> RULES = Map.of(
            "/api/v1/auth/login",           new RateRule(5,  Duration.ofSeconds(10)),
            "/api/v1/auth/register",        new RateRule(5,  Duration.ofSeconds(10)),
            "/api/v1/auth/magic-link/send", new RateRule(3,  Duration.ofSeconds(60)),
            "/api/v1/auth/verify-otp",      new RateRule(10, Duration.ofMinutes(5))
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        RateRule rule = RULES.get(request.getRequestURI());
        if (rule == null || !"POST".equals(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String ip = resolveIp(request);
        String key = "rate:" + request.getRequestURI() + ":" + ip;

        Long count = sessionRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            sessionRedisTemplate.expire(key, rule.window());
        }

        if (count != null && count > rule.maxRequests()) {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"Quá nhiều yêu cầu, vui lòng thử lại sau.\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isBlank())
                ? forwarded.split(",")[0].trim()
                : request.getRemoteAddr();
    }
}
