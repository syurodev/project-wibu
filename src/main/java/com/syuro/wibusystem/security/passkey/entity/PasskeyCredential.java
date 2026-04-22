package com.syuro.wibusystem.security.passkey.entity;

import com.syuro.wibusystem.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(
        name = "passkey_credentials",
        schema = "identity",
        uniqueConstraints = @UniqueConstraint(columnNames = "credential_id")
)
public class PasskeyCredential extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "credential_id", nullable = false, columnDefinition = "bytea")
    private byte[] credentialId;

    @Column(name = "public_key_cose", nullable = false, columnDefinition = "bytea")
    private byte[] publicKeyCose;

    @Column(name = "sign_count", nullable = false)
    private long signCount;

    @Column(name = "aaguid", length = 36)
    private String aaguid;

    @Column(name = "transports", columnDefinition = "text")
    private String transports;

    @Column(name = "friendly_name", length = 100)
    private String friendlyName;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;
}
