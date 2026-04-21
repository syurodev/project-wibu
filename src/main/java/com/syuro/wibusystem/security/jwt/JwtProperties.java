package com.syuro.wibusystem.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Ánh xạ các cấu hình JWT từ application.yaml (prefix: app.jwt).
 *
 * secret            : khóa bí mật dùng để ký JWT, encode bằng Base64 (tối thiểu 256 bit)
 * accessTokenExpiry : thời gian sống của access token tính bằng giây (mặc định 900 = 15 phút)
 * refreshTokenExpiry: thời gian sống của refresh token tính bằng giây (mặc định 604800 = 7 ngày)
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        long accessTokenExpiry,
        long refreshTokenExpiry
) {}
