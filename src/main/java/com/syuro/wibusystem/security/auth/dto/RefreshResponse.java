package com.syuro.wibusystem.security.auth.dto;

public record RefreshResponse(String accessToken, String refreshToken, long expiresIn) {
}
