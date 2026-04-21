package com.syuro.wibusystem.security.session.dto;

import java.time.Instant;

public record SessionResponse(
        Long id,
        String deviceUserAgent,
        String ipAddress,
        Instant createdAt,
        Instant expiresAt,
        boolean current
) {
}
