package com.syuro.wibusystem.security.session.dto;

import java.util.List;

public record SessionCachePayload(
        Long userId,
        String email,
        String name,
        List<String> roles,
        List<String> permissions,
        String version,
        long expiresAt
) {
    public SessionCachePayload withExpiresAt(long newExpiresAt) {
        return new SessionCachePayload(userId, email, name, roles, permissions, version, newExpiresAt);
    }
}
