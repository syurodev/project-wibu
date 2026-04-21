package com.syuro.wibusystem.shared.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.List;

/**
 * Cấu hình CORS toàn cục, được Spring Security nhặt qua .cors(Customizer.withDefaults())
 * (bean tên corsConfigurationSource).
 *
 * Lưu ý:
 *   - allowCredentials=true KHÔNG đi chung với allowedOrigins=["*"] → dùng allowedOriginPatterns
 *   - Preflight OPTIONS đi qua CorsFilter trước chain security nên không cần JWT
 */
@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {

    private static final List<String> DEFAULT_METHODS =
            List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    private static final List<String> DEFAULT_HEADERS = List.of("*");
    private static final Duration DEFAULT_MAX_AGE = Duration.ofHours(1);

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties props) {
        CorsConfiguration cfg = new CorsConfiguration();

        if (props.allowedOrigins() != null && !props.allowedOrigins().isEmpty()) {
            cfg.setAllowedOrigins(props.allowedOrigins());
        }
        if (props.allowedOriginPatterns() != null && !props.allowedOriginPatterns().isEmpty()) {
            cfg.setAllowedOriginPatterns(props.allowedOriginPatterns());
        }

        cfg.setAllowedMethods(
                props.allowedMethods() != null && !props.allowedMethods().isEmpty()
                        ? props.allowedMethods() : DEFAULT_METHODS);
        cfg.setAllowedHeaders(
                props.allowedHeaders() != null && !props.allowedHeaders().isEmpty()
                        ? props.allowedHeaders() : DEFAULT_HEADERS);

        if (props.exposedHeaders() != null && !props.exposedHeaders().isEmpty()) {
            cfg.setExposedHeaders(props.exposedHeaders());
        }

        cfg.setAllowCredentials(props.allowCredentials());
        cfg.setMaxAge(props.maxAge() != null ? props.maxAge() : DEFAULT_MAX_AGE);

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }
}
