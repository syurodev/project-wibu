package com.syuro.wibusystem.security.passkey.service;

import com.syuro.wibusystem.rbac.api.PermissionChecker;
import com.syuro.wibusystem.security.auth.dto.LoginResponse;
import com.syuro.wibusystem.security.jwt.JwtProperties;
import com.syuro.wibusystem.security.jwt.JwtService;
import com.syuro.wibusystem.security.passkey.dto.PasskeyAuthBeginResponse;
import com.syuro.wibusystem.security.passkey.repository.PasskeyCredentialJpaRepository;
import com.syuro.wibusystem.security.passkey.repository.PasskeyCredentialRepository;
import com.syuro.wibusystem.security.session.entity.Session;
import com.syuro.wibusystem.security.session.repository.SessionRepository;
import com.syuro.wibusystem.shared.exception.AppException;
import com.syuro.wibusystem.shared.exception.ErrorCode;
import com.syuro.wibusystem.user.api.UserProfile;
import com.syuro.wibusystem.user.api.UserQueryService;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.UserVerificationRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperties.class)
public class PasskeyAuthenticationService {

    private final RelyingParty relyingParty;
    private final PasskeyChallengeService challengeService;
    private final PasskeyCredentialJpaRepository jpaRepository;
    private final UserQueryService userQueryService;
    private final PermissionChecker permissionChecker;
    private final SessionRepository sessionRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final tools.jackson.databind.ObjectMapper jacksonMapper;

    // Tạo assertion challenge. Nếu có email → gửi kèm allowCredentials (non-discoverable).
    // Nếu không có email → allowCredentials rỗng → browser tự hiện danh sách passkey (discoverable).
    public PasskeyAuthBeginResponse beginAuthentication(String email) {
        StartAssertionOptions.StartAssertionOptionsBuilder builder = StartAssertionOptions.builder()
                .userVerification(UserVerificationRequirement.PREFERRED);

        if (email != null && !email.isBlank()) {
            builder.username(email);
        }

        var assertionRequest = relyingParty.startAssertion(builder.build());
        String sessionKey = challengeService.storeAuthentication(assertionRequest);

        try {
            // Bridge Yubico JSON (com.fasterxml) → tools.jackson JsonNode để Spring MVC serialize đúng
            JsonNode optionsNode = jacksonMapper.readTree(assertionRequest.toJson());
            return new PasskeyAuthBeginResponse(sessionKey, optionsNode);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize assertion request", e);
        }
    }

    // Verify assertion signature, cập nhật signCount chống replay attack, tạo session mới.
    // Flow giống login thông thường — trả về cùng LoginResponse.
    @Transactional
    public LoginResponse completeAuthentication(String sessionKey, String credentialJson,
                                                String userAgent, String ipAddress) {
        var assertionRequest = challengeService.loadAndDeleteAuthentication(sessionKey);

        AssertionResult result;
        try {
            PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> pkc =
                    PublicKeyCredential.parseAssertionResponseJson(credentialJson);
            result = relyingParty.finishAssertion(
                    FinishAssertionOptions.builder()
                            .request(assertionRequest)
                            .response(pkc)
                            .build()
            );
        } catch (Exception e) {
            throw new AppException(ErrorCode.PASSKEY_VERIFICATION_FAILED);
        }

        if (!result.isSuccess()) {
            throw new AppException(ErrorCode.PASSKEY_VERIFICATION_FAILED);
        }

        Long userId = PasskeyCredentialRepository.decodeUserHandle(result.getUserHandle());
        updateSignCount(result.getCredential().getCredentialId().getBytes(), result.getSignatureCount());

        UserProfile user = userQueryService.findProfileById(userId);
        List<String> roles = permissionChecker.getGlobalRoleNames(userId);
        List<String> permissions = permissionChecker.getExpandedPermissions(userId);

        String rawRefreshToken = generateRefreshToken();
        Session session = sessionRepository.save(Session.builder()
                .userId(userId)
                .refreshTokenHash(hashToken(rawRefreshToken))
                .deviceUserAgent(userAgent)
                .ipAddress(ipAddress)
                .expiresAt(Instant.now().plusSeconds(jwtProperties.refreshTokenExpiry()))
                .build());

        String accessToken = jwtService.generateAccessToken(
                userId, session.getId(), user.email(), user.name(), permissions);

        return new LoginResponse(accessToken, rawRefreshToken, jwtProperties.accessTokenExpiry(),
                user, roles, permissions);
    }

    // signCount tăng mỗi lần dùng — nếu server nhận signCount thấp hơn đã lưu thì credential bị clone
    private void updateSignCount(byte[] credentialIdBytes, long newSignCount) {
        jpaRepository.findByCredentialId(credentialIdBytes).ifPresent(c -> {
            c.setSignCount(newSignCount);
            c.setLastUsedAt(Instant.now());
            jpaRepository.save(c);
        });
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String raw) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
