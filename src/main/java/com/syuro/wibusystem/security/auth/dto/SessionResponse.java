package com.syuro.wibusystem.security.auth.dto;

import com.syuro.wibusystem.user.api.UserProfile;

import java.util.List;

public record SessionResponse(
        String sessionData,
        String sessionToken,
        long expiresIn,
        UserProfile user,
        List<String> roles,
        List<String> permissions
) {
}
