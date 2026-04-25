package com.syuro.wibusystem.security.auth.controller.v1;

import com.syuro.wibusystem.security.auth.dto.*;
import com.syuro.wibusystem.security.auth.service.AuthService;
import com.syuro.wibusystem.security.rsa.RsaKeyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RsaKeyService rsaKeyService;

    @GetMapping("/public-key")
    public ResponseEntity<Map<String, String>> getPublicKey() {
        return ResponseEntity.ok(Map.of("public_key", rsaKeyService.getPublicKeyBase64()));
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request, httpRequest));
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

    @GetMapping("/session")
    public ResponseEntity<SessionResponse> getSession(
            @RequestHeader("X-Session-Token") String signedToken) {
        return ResponseEntity.ok(authService.getSessionInfo(signedToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "X-Session-Token", required = false) String signedToken) {
        authService.logout(signedToken);
        return ResponseEntity.noContent().build();
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
