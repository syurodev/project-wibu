package com.syuro.wibusystem.security.session.api;

import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.util.List;

public record SessionCachePayload(
        @JsonSerialize(using = ToStringSerializer.class)
        Long userId,
        String email,
        String name,
        List<String> roles,
        List<String> permissions,
        String version,
        long expiresAt,
        @JsonSerialize(using = ToStringSerializer.class)
        Long creatorProfileId
) {
    public SessionCachePayload withExpiresAt(long newExpiresAt) {
        return new SessionCachePayload(userId, email, name, roles, permissions, version, newExpiresAt, creatorProfileId);
    }
}
