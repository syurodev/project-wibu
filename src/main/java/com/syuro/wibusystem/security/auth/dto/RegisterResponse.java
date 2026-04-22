package com.syuro.wibusystem.security.auth.dto;

import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

public record RegisterResponse(
        @JsonSerialize(using = ToStringSerializer.class)
        Long userId,
        String email) {
}