package com.syuro.wibusystem.user.entity;

import com.syuro.wibusystem.shared.entity.BaseEntity;
import com.syuro.wibusystem.user.api.AccountProvider;
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
        name = "accounts",
        schema = "identity",
        uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_account_id"})
)
public class Account extends BaseEntity {
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountProvider provider;

    @Column(unique = false, nullable = true, name = "provider_email")
    private String providerEmail;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    // OAuth2: ID từ provider. CREDENTIAL: null
    @Column(name = "provider_account_id")
    private String providerAccountId;

    // CREDENTIAL only — BCrypt hash
    @Column(name = "password_hash")
    private String passwordHash;

    // OAuth2 only
    @Column(name = "access_token", columnDefinition = "text")
    private String accessToken;

    @Column(name = "refresh_token", columnDefinition = "text")
    private String oauthRefreshToken;

    @Column(name = "expires_at")
    private Instant expiresAt;
}
