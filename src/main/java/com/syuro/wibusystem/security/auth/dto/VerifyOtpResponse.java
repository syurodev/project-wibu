package com.syuro.wibusystem.security.auth.dto;

import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

public record VerifyOtpResponse(
        @JsonSerialize(using = ToStringSerializer.class)
        Long id, String name,
        String email) {
}
