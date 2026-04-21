package com.syuro.wibusystem.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

/**
 * Ánh xạ cấu hình CORS từ application.yaml (prefix: app.cors).
 *
 * allowedOrigins        : danh sách origin cụ thể được phép (ví dụ: https://app.example.com)
 * allowedOriginPatterns : pattern origin khi muốn dùng wildcard kèm allowCredentials=true
 * allowedMethods        : HTTP method được phép (GET, POST, ...)
 * allowedHeaders        : header client được gửi lên
 * exposedHeaders        : header browser có thể đọc từ response (ví dụ: Authorization)
 * allowCredentials      : cho phép gửi cookie / Authorization header kèm request
 * maxAge                : thời gian browser cache preflight response
 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(
        List<String> allowedOrigins,
        List<String> allowedOriginPatterns,
        List<String> allowedMethods,
        List<String> allowedHeaders,
        List<String> exposedHeaders,
        boolean allowCredentials,
        Duration maxAge
) {}
