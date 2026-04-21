package com.syuro.wibusystem.shared.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Cấu hình Spring Cache với Redis backend.
 *
 * Dùng: @Cacheable(cacheNames = "user-profile")
 * TTL per cache name khai báo trong app.redis.cache-ttl.*
 * Fallback: 30 phút nếu cache name chưa được khai báo.
 *
 * Key format: "cache:{cacheName}::{cacheKey}"
 */
@Configuration
@EnableCaching
public class CacheConfig {

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);

    @Bean
    public CacheManager cacheManager(
            @Qualifier("cacheRedisConnectionFactory") RedisConnectionFactory factory,
            RedisProperties props) {

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(RedisConfig.buildJsonSerializer()))
                .prefixCacheNameWith("cache:")
                .disableCachingNullValues()
                .entryTtl(DEFAULT_TTL);

        Map<String, RedisCacheConfiguration> perCacheConfig = new HashMap<>();
        if (props.cacheTtl() != null) {
            props.cacheTtl().forEach((name, ttl) ->
                    perCacheConfig.put(name, defaultConfig.entryTtl(ttl)));
        }

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(perCacheConfig)
                .build();
    }
}