package com.syuro.wibusystem.security.passkey.repository;

import com.syuro.wibusystem.security.passkey.entity.PasskeyCredential;
import com.syuro.wibusystem.user.api.AccountQueryService;
import com.syuro.wibusystem.user.api.UserQueryService;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PasskeyCredentialRepository implements CredentialRepository {

    private final PasskeyCredentialJpaRepository jpaRepository;
    private final AccountQueryService accountQueryService;
    private final UserQueryService userQueryService;

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        return accountQueryService.findCredentialByEmail(username)
                .map(account -> jpaRepository.findAllByUserId(account.userId()).stream()
                        .map(c -> PublicKeyCredentialDescriptor.builder()
                                .id(new ByteArray(c.getCredentialId()))
                                .build())
                        .collect(Collectors.toSet()))
                .orElse(Set.of());
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        return accountQueryService.findCredentialByEmail(username)
                .map(account -> encodeUserHandle(account.userId()));
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        Long userId = decodeUserHandle(userHandle);
        try {
            return Optional.of(userQueryService.findById(userId).email());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        Long userId = decodeUserHandle(userHandle);
        return jpaRepository.findByCredentialIdAndUserId(credentialId.getBytes(), userId)
                .map(c -> toRegisteredCredential(c, userHandle));
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        return jpaRepository.findByCredentialId(credentialId.getBytes())
                .map(c -> toRegisteredCredential(c, encodeUserHandle(c.getUserId())))
                .map(Set::of)
                .orElse(Set.of());
    }

    private RegisteredCredential toRegisteredCredential(PasskeyCredential c, ByteArray userHandle) {
        return RegisteredCredential.builder()
                .credentialId(new ByteArray(c.getCredentialId()))
                .userHandle(userHandle)
                .publicKeyCose(new ByteArray(c.getPublicKeyCose()))
                .signatureCount(c.getSignCount())
                .build();
    }

    public static ByteArray encodeUserHandle(Long userId) {
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.putLong(userId);
        return new ByteArray(buf.array());
    }

    public static Long decodeUserHandle(ByteArray handle) {
        return ByteBuffer.wrap(handle.getBytes()).getLong();
    }
}
