package com.syuro.wibusystem.security.session.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.session")
public record SessionProperties(
        List<String> secrets,
        long expiresIn,
        long updateAge,
        long cacheTtl
) {
    public String primarySecret() {
        return secrets.get(0);
    }
}
