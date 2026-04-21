package com.syuro.wibusystem.security;

import com.syuro.wibusystem.security.jwt.JwtAuthFilter;
import com.syuro.wibusystem.security.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Cấu hình Spring Security cho toàn bộ ứng dụng.
 *
 * Chiến lược xác thực:
 *   - STATELESS: server không lưu session, mỗi request phải mang JWT theo
 *   - CSRF disabled: không cần thiết cho REST API dùng token
 *   - JwtAuthFilter chạy trước UsernamePasswordAuthenticationFilter để xử lý Bearer token
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class) // kích hoạt binding app.jwt.* → JwtProperties
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // Bật CORS — dùng CorsConfigurationSource bean (xem CorsConfig)
                .cors(Customizer.withDefaults())
                // Tắt CSRF — không cần thiết với REST API dùng JWT
                .csrf(AbstractHttpConfigurer::disable)
                // Không dùng session phía server
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Các endpoint công khai: đăng ký, đăng nhập, refresh, swagger
                        .requestMatchers("/v1/auth/**", "/v1/sessions/anonymous", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Tất cả các endpoint còn lại yêu cầu xác thực
                        .anyRequest().authenticated()
                )
                // Đặt JwtAuthFilter trước filter xác thực mặc định
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * BCrypt encoder để hash mật khẩu khi đăng ký và verify khi đăng nhập.
     * Cost factor mặc định là 10 — đủ mạnh cho hầu hết ứng dụng.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Expose AuthenticationManager như một bean để AuthService có thể inject nếu cần
     * (ví dụ: xác thực thủ công qua UsernamePasswordAuthenticationToken).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
