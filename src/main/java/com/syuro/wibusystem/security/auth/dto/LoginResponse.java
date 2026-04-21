package com.syuro.wibusystem.security.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.syuro.wibusystem.user.api.UserProfile;

import java.util.List;

public record LoginResponse(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("expires_in")
        long expiresIn,

        UserProfile user,

        List<String> roles,

        List<String> permissions
) {
}
