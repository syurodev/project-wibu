package com.syuro.wibusystem.security.passkey.service;

import com.syuro.wibusystem.security.passkey.dto.PasskeyCredentialView;
import com.syuro.wibusystem.security.passkey.dto.PasskeyRegisterBeginResponse;
import com.syuro.wibusystem.security.passkey.dto.PasskeyRegisterCompleteResponse;
import com.syuro.wibusystem.security.passkey.entity.PasskeyCredential;
import com.syuro.wibusystem.security.passkey.repository.PasskeyCredentialJpaRepository;
import com.syuro.wibusystem.security.passkey.repository.PasskeyCredentialRepository;
import com.syuro.wibusystem.shared.exception.AppException;
import com.syuro.wibusystem.shared.exception.ErrorCode;
import com.syuro.wibusystem.user.api.UserQueryService;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PasskeyRegistrationService {

    private final RelyingParty relyingParty;
    private final PasskeyChallengeService challengeService;
    private final PasskeyCredentialJpaRepository jpaRepository;
    private final UserQueryService userQueryService;
    private final tools.jackson.databind.ObjectMapper jacksonMapper;

    // Tạo challenge và trả về PublicKeyCredentialCreationOptions cho browser.
    // ResidentKey REQUIRED để hỗ trợ discoverable credentials (đăng nhập không cần nhập email).
    public PasskeyRegisterBeginResponse beginRegistration(Long userId, String friendlyName) {
        var user = userQueryService.findById(userId);

        PublicKeyCredentialCreationOptions options = relyingParty.startRegistration(
                StartRegistrationOptions.builder()
                        .user(UserIdentity.builder()
                                .name(user.email())
                                .displayName(user.name())
                                .id(PasskeyCredentialRepository.encodeUserHandle(userId))
                                .build())
                        .authenticatorSelection(AuthenticatorSelectionCriteria.builder()
                                .residentKey(ResidentKeyRequirement.REQUIRED)
                                .userVerification(UserVerificationRequirement.PREFERRED)
                                .build())
                        .build()
        );

        challengeService.storeRegistration(userId, options);

        try {
            // Yubico serialize bằng Jackson 2.x — bridge sang tools.jackson JsonNode để Spring MVC trả về đúng
            JsonNode optionsNode = jacksonMapper.readTree(options.toJson());
            return new PasskeyRegisterBeginResponse(optionsNode);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize registration options", e);
        }
    }

    // Verify attestation từ authenticator, lưu credential (credentialId + public key + signCount).
    // Challenge được consume ngay sau khi load — không dùng lại được.
    @Transactional
    public PasskeyRegisterCompleteResponse completeRegistration(
            Long userId, String credentialJson, String friendlyName) {

        PublicKeyCredentialCreationOptions options = challengeService.loadAndDeleteRegistration(userId);

        RegistrationResult result;
        try {
            PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> pkc =
                    PublicKeyCredential.parseRegistrationResponseJson(credentialJson);
            result = relyingParty.finishRegistration(
                    FinishRegistrationOptions.builder()
                            .request(options)
                            .response(pkc)
                            .build()
            );
        } catch (Exception e) {
            throw new AppException(ErrorCode.PASSKEY_VERIFICATION_FAILED);
        }

        if (jpaRepository.existsByCredentialId(result.getKeyId().getId().getBytes())) {
            throw new AppException(ErrorCode.PASSKEY_CREDENTIAL_ALREADY_EXISTS);
        }

        PasskeyCredential credential = PasskeyCredential.builder()
                .userId(userId)
                .credentialId(result.getKeyId().getId().getBytes())
                .publicKeyCose(result.getPublicKeyCose().getBytes())
                .signCount(result.getSignatureCount())
                .aaguid(result.getAaguid().getHex())
                .transports(serializeTransports(result))
                .friendlyName(friendlyName)
                .build();

        jpaRepository.save(credential);

        return new PasskeyRegisterCompleteResponse(
                credential.getId(),
                credential.getFriendlyName(),
                credential.getCreatedAt()
        );
    }

    public List<PasskeyCredentialView> listCredentials(Long userId) {
        return jpaRepository.findAllByUserId(userId).stream()
                .map(c -> new PasskeyCredentialView(
                        c.getId(),
                        c.getFriendlyName(),
                        deserializeTransports(c.getTransports()),
                        c.getCreatedAt(),
                        c.getLastUsedAt()
                ))
                .toList();
    }

    @Transactional
    public void deleteCredential(Long userId, Long credentialId) {
        PasskeyCredential credential = jpaRepository.findById(credentialId)
                .filter(c -> c.getUserId().equals(userId))
                .orElseThrow(() -> new AppException(ErrorCode.PASSKEY_CREDENTIAL_NOT_FOUND));
        jpaRepository.delete(credential);
    }

    // Transports (internal, hybrid, usb...) lưu dưới dạng JSON array string trong DB
    private String serializeTransports(RegistrationResult result) {
        try {
            var transports = result.getKeyId().getTransports()
                    .map(set -> set.stream().map(AuthenticatorTransport::getId).toList())
                    .orElse(List.of());
            return new ObjectMapper().writeValueAsString(transports);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> deserializeTransports(String json) {
        if (json == null) return List.of();
        try {
            return new ObjectMapper().readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
