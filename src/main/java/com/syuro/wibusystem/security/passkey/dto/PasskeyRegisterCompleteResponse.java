package com.syuro.wibusystem.security.passkey.dto;

import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.time.Instant;

public record PasskeyRegisterCompleteResponse(
        @JsonSerialize(using = ToStringSerializer.class)
        Long credentialId,
        String friendlyName,
        Instant createdAt
) {
}
