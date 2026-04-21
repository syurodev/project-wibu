package com.syuro.wibusystem.security.session.repository;

import com.syuro.wibusystem.security.session.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findByRefreshTokenHash(String hash);

    List<Session> findAllByUserIdAndRevokedAtIsNull(Long userId);

    Optional<Session> findByIdAndUserId(Long id, Long userId);

    @Modifying
    @Query("UPDATE Session s SET s.revokedAt = :now WHERE s.userId = :userId AND s.revokedAt IS NULL")
    void revokeAllByUserId(Long userId, Instant now);
}
