package com.syuro.wibusystem.security.passkey.dto;

import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.time.Instant;
import java.util.List;

public record PasskeyCredentialView(
        @JsonSerialize(using = ToStringSerializer.class) Long id,
        String friendlyName,
        List<String> transports,
        Instant createdAt,
        Instant lastUsedAt
) {}
