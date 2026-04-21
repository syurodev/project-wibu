package com.syuro.wibusystem.security.session.anonymous;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("app.anonymous-session")
public record AnonymousSessionProperties(
        Duration ttl,
        int rateLimitPerHour,
        int activityThreshold
) {}
