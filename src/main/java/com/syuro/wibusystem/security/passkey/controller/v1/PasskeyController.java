package com.syuro.wibusystem.security.passkey.controller.v1;

import com.syuro.wibusystem.security.auth.dto.LoginResponse;
import com.syuro.wibusystem.security.passkey.dto.*;
import com.syuro.wibusystem.security.passkey.service.PasskeyAuthenticationService;
import com.syuro.wibusystem.security.passkey.service.PasskeyRegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;

@RestController
@RequestMapping("/v1/auth/passkey")
@RequiredArgsConstructor
public class PasskeyController {

    private final PasskeyRegistrationService registrationService;
    private final PasskeyAuthenticationService authenticationService;
    private final ObjectMapper jacksonMapper;

    // ─── Registration (requires JWT) ─────────────────────────────────────────

    @PostMapping("/register/begin")
    public ResponseEntity<PasskeyRegisterBeginResponse> beginRegistration(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PasskeyRegisterBeginRequest request) {
        return ResponseEntity.ok(registrationService.beginRegistration(userId, request.getFriendlyName()));
    }

    @PostMapping("/register/complete")
    public ResponseEntity<PasskeyRegisterCompleteResponse> completeRegistration(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PasskeyRegisterCompleteRequest request) throws Exception {
        String credentialJson = jacksonMapper.writeValueAsString(normalizeCredential(request.getCredential()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(registrationService.completeRegistration(userId, credentialJson, null));
    }

    // ─── Authentication (public) ──────────────────────────────────────────────

    @PostMapping("/authenticate/begin")
    public ResponseEntity<PasskeyAuthBeginResponse> beginAuthentication(
            @Valid @RequestBody PasskeyAuthBeginRequest request) {
        return ResponseEntity.ok(authenticationService.beginAuthentication(request.getEmail()));
    }

    @PostMapping("/authenticate/complete")
    public ResponseEntity<LoginResponse> completeAuthentication(
            @Valid @RequestBody PasskeyAuthCompleteRequest request,
            HttpServletRequest httpRequest) throws Exception {
        String userAgent = httpRequest.getHeader("User-Agent");
        String ip = resolveClientIp(httpRequest);
        String credentialJson = jacksonMapper.writeValueAsString(normalizeCredential(request.getCredential()));
        return ResponseEntity.ok(authenticationService.completeAuthentication(
                request.getSessionKey(), credentialJson, userAgent, ip));
    }

    // ─── Credential management (requires JWT) ────────────────────────────────

    @GetMapping("/credentials")
    public ResponseEntity<List<PasskeyCredentialView>> listCredentials(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(registrationService.listCredentials(userId));
    }

    @DeleteMapping("/credentials/{id}")
    public ResponseEntity<Void> deleteCredential(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        registrationService.deleteCredential(userId, id);
        return ResponseEntity.noContent().build();
    }

    // Browser thường không gửi clientExtensionResults — Yubico yêu cầu field này non-null
    private ObjectNode normalizeCredential(ObjectNode credential) {
        if (!credential.has("clientExtensionResults")) {
            credential.putObject("clientExtensionResults");
        }
        return credential;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
