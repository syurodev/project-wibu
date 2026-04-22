package com.syuro.wibusystem.security.passkey.repository;

import com.syuro.wibusystem.security.passkey.entity.PasskeyCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasskeyCredentialJpaRepository extends JpaRepository<PasskeyCredential, Long> {

    List<PasskeyCredential> findAllByUserId(Long userId);

    Optional<PasskeyCredential> findByCredentialId(byte[] credentialId);

    Optional<PasskeyCredential> findByCredentialIdAndUserId(byte[] credentialId, Long userId);

    boolean existsByCredentialId(byte[] credentialId);
}
