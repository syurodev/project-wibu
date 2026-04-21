package com.syuro.wibusystem.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Map;

@ConfigurationProperties("app.redis")
public record RedisProperties(
        ConnectionProperties session,
        ConnectionProperties cache,
        Map<String, Duration> cacheTtl
) {

    public record ConnectionProperties(
            String host,
            int port,
            String password,
            int database,
            PoolProperties pool
    ) {}

    public record PoolProperties(
            int maxActive,
            int maxIdle,
            int minIdle,
            Duration maxWait
    ) {}
}