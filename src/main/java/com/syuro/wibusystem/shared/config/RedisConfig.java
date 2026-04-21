package com.syuro.wibusystem.shared.config;

import io.lettuce.core.api.StatefulConnection;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import com.syuro.wibusystem.security.session.anonymous.AnonymousSessionProperties;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

/**
 * Cấu hình 2 connection factory trỏ cùng Redis instance (dev).
 * Prod: đổi app.redis.cache.host/port sang instance riêng, database: 0.
 *
 * - sessionRedisConnectionFactory (@Primary) — db 0
 * - cacheRedisConnectionFactory              — db 1
 */
@Configuration
@EnableConfigurationProperties({RedisProperties.class, AnonymousSessionProperties.class})
public class RedisConfig {

    @Bean
    @Primary
    public LettuceConnectionFactory sessionRedisConnectionFactory(RedisProperties props) {
        return buildFactory(props.session());
    }

    @Bean
    public LettuceConnectionFactory cacheRedisConnectionFactory(RedisProperties props) {
        return buildFactory(props.cache());
    }

    /**
     * Template dùng cho session store và các thao tác Redis thủ công.
     * Key: String, Value: JSON với type metadata để deserialize đúng kiểu polymorphic.
     */
    @Bean
    @Primary
    public RedisTemplate<String, Object> sessionRedisTemplate(
            @Qualifier("sessionRedisConnectionFactory") RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        StringRedisSerializer string = new StringRedisSerializer();
        RedisSerializer<Object> json = buildJsonSerializer();

        template.setKeySerializer(string);
        template.setHashKeySerializer(string);
        template.setValueSerializer(json);
        template.setHashValueSerializer(json);

        return template;
    }

    /**
     * Jackson 3-based serializer — thay thế GenericJackson2JsonRedisSerializer (deprecated Spring Data Redis 4.0).
     * enableDefaultTyping ghi @type metadata vào JSON để deserialize đúng kiểu polymorphic.
     * allowIfSubType(Object.class) — internal Redis store, không expose ra ngoài.
     */
    static RedisSerializer<Object> buildJsonSerializer() {
        return GenericJacksonJsonRedisSerializer.builder()
                .enableDefaultTyping(
                        BasicPolymorphicTypeValidator.builder()
                                .allowIfSubType(Object.class)
                                .build()
                )
                .build();
    }

    private LettuceConnectionFactory buildFactory(RedisProperties.ConnectionProperties props) {
        RedisStandaloneConfiguration server = new RedisStandaloneConfiguration();
        server.setHostName(props.host());
        server.setPort(props.port());
        server.setPassword(RedisPassword.of(props.password()));
        server.setDatabase(props.database());

        GenericObjectPoolConfig<StatefulConnection<?, ?>> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(props.pool().maxActive());
        poolConfig.setMaxIdle(props.pool().maxIdle());
        poolConfig.setMinIdle(props.pool().minIdle());
        poolConfig.setMaxWait(props.pool().maxWait());

        LettucePoolingClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .poolConfig(poolConfig)
                .build();

        return new LettuceConnectionFactory(server, clientConfig);
    }
}