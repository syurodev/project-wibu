package com.syuro.wibusystem.security.session.dto;

import com.syuro.wibusystem.security.session.entity.Session;

import java.time.Instant;

public record SessionResponse(
        Long id,
        String deviceUserAgent,
        String ipAddress,
        Instant createdAt,
        Instant expiresAt,
        boolean current
) {
    public static SessionResponse from(Session session, Long currentSessionId) {
        return new SessionResponse(
                session.getId(),
                session.getDeviceUserAgent(),
                session.getIpAddress(),
                session.getCreatedAt(),
                session.getExpiresAt(),
                session.getId().equals(currentSessionId)
        );
    }
}
