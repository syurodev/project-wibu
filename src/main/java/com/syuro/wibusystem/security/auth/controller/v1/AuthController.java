package com.syuro.wibusystem.security.auth.controller.v1;

import com.syuro.wibusystem.security.auth.dto.*;
import com.syuro.wibusystem.security.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<VerifyOtpResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        String userAgent = httpRequest.getHeader("User-Agent");
        String ip = resolveClientIp(httpRequest);
        return ResponseEntity.ok(authService.login(request, userAgent, ip));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(
            @Valid @RequestBody RefreshRequest request,
            HttpServletRequest httpRequest) {
        String userAgent = httpRequest.getHeader("User-Agent");
        String ip = resolveClientIp(httpRequest);
        return ResponseEntity.ok(authService.refresh(request, userAgent, ip));
    }

    @PostMapping("/magic-link/send")
    public ResponseEntity<MagicLinkSendResponse> sendMagicLink(@Valid @RequestBody MagicLinkSendRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(authService.sendMagicLink(request));
    }

    @PostMapping("/magic-link/verify")
    public ResponseEntity<LoginResponse> verifyMagicLink(
            @Valid @RequestBody MagicLinkVerifyRequest request,
            HttpServletRequest httpRequest) {
        String userAgent = httpRequest.getHeader("User-Agent");
        String ip = resolveClientIp(httpRequest);
        return ResponseEntity.ok(authService.verifyMagicLink(request, userAgent, ip));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
