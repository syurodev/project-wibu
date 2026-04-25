package com.syuro.wibusystem.security.auth.dto;

public record PendingRegistration(
        String otp,
        String email,
        String name,
        String passwordHash,
        String language
) {}
