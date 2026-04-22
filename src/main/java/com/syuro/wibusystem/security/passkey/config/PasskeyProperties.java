package com.syuro.wibusystem.security.passkey.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties(prefix = "app.passkey")
public record PasskeyProperties(
        String rpId,
        String rpName,
        Set<String> origins,
        long challengeTtlSeconds
) {}
