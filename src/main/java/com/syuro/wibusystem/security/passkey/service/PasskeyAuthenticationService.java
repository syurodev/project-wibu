package com.syuro.wibusystem.security.passkey.service;

import com.syuro.wibusystem.security.auth.dto.LoginResponse;
import com.syuro.wibusystem.security.auth.service.AuthService;
import com.syuro.wibusystem.security.passkey.dto.PasskeyAuthBeginResponse;
import com.syuro.wibusystem.security.passkey.repository.PasskeyCredentialJpaRepository;
import com.syuro.wibusystem.security.passkey.repository.PasskeyCredentialRepository;
import com.syuro.wibusystem.shared.exception.AppException;
import com.syuro.wibusystem.shared.exception.ErrorCode;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.UserVerificationRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PasskeyAuthenticationService {

    private final RelyingParty relyingParty;
    private final PasskeyChallengeService challengeService;
    private final PasskeyCredentialJpaRepository jpaRepository;
    private final AuthService authService;
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

        return authService.createSession(userId, userAgent, ipAddress);
    }

    // signCount tăng mỗi lần dùng — nếu server nhận signCount thấp hơn đã lưu thì credential bị clone
    private void updateSignCount(byte[] credentialIdBytes, long newSignCount) {
        jpaRepository.findByCredentialId(credentialIdBytes).ifPresent(c -> {
            c.setSignCount(newSignCount);
            c.setLastUsedAt(Instant.now());
            jpaRepository.save(c);
        });
    }
}
