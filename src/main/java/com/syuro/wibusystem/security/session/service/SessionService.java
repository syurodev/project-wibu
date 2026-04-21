package com.syuro.wibusystem.security.session.service;

import com.syuro.wibusystem.security.jwt.JwtProperties;
import com.syuro.wibusystem.security.jwt.TokenBlacklistService;
import com.syuro.wibusystem.security.session.dto.SessionResponse;
import com.syuro.wibusystem.security.session.entity.Session;
import com.syuro.wibusystem.security.session.repository.SessionRepository;
import com.syuro.wibusystem.shared.exception.AppException;
import com.syuro.wibusystem.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtProperties jwtProperties;

    @Transactional(readOnly = true)
    public List<SessionResponse> listActive(Long userId, Long currentSessionId) {
        Instant now = Instant.now();
        return sessionRepository.findAllByUserIdAndRevokedAtIsNull(userId).stream()
                .filter(s -> s.getExpiresAt().isAfter(now))
                .map(s -> new SessionResponse(
                        s.getId(),
                        s.getDeviceUserAgent(),
                        s.getIpAddress(),
                        s.getCreatedAt(),
                        s.getExpiresAt(),
                        s.getId().equals(currentSessionId)
                ))
                .toList();
    }

    @Transactional
    public void removeSession(Long userId, Long sessionId) {
        Session session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND));

        session.setRevokedAt(Instant.now());
        tokenBlacklistService.blacklist(session.getId(), Duration.ofSeconds(jwtProperties.accessTokenExpiry()));
    }

    @Transactional
    public void removeAllSessions(Long userId) {
        List<Session> active = sessionRepository.findAllByUserIdAndRevokedAtIsNull(userId);
        Instant now = Instant.now();
        Duration ttl = Duration.ofSeconds(jwtProperties.accessTokenExpiry());

        active.forEach(s -> {
            s.setRevokedAt(now);
            tokenBlacklistService.blacklist(s.getId(), ttl);
        });
    }
}
