package com.syuro.wibusystem.user.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.util.Map;

public record UserProfile(
        @JsonSerialize(using = ToStringSerializer.class)
        Long id,

        String name,

        @JsonProperty("another_name")
        String anotherName,

        String email,

        String avatar,

        Map<String, Object> settings
) {}
