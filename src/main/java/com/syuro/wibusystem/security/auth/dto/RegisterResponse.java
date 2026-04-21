package com.syuro.wibusystem.security.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

public record RegisterResponse(
        @JsonProperty("user_id")
        @JsonSerialize(using = ToStringSerializer.class)
        Long userId,
        String email) {
}