package com.syuro.wibusystem.security.session.service;

import com.syuro.wibusystem.security.session.dto.SessionResponse;
import com.syuro.wibusystem.security.session.entity.Session;
import com.syuro.wibusystem.security.session.repository.SessionRepository;
import com.syuro.wibusystem.shared.exception.AppException;
import com.syuro.wibusystem.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;

    @Transactional(readOnly = true)
    public List<SessionResponse> listActive(Long userId, Long currentSessionId) {
        Instant now = Instant.now();
        return sessionRepository.findAllByUserIdAndRevokedAtIsNull(userId).stream()
                .filter(s -> s.getExpiresAt().isAfter(now))
                .map(s -> SessionResponse.from(s, currentSessionId))
                .toList();
    }

    @Transactional
    public void removeSession(Long userId, Long sessionId) {
        Session session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND));
        session.setRevokedAt(Instant.now());
    }

    @Transactional
    public void removeAllSessions(Long userId) {
        Instant now = Instant.now();
        sessionRepository.findAllByUserIdAndRevokedAtIsNull(userId)
                .forEach(s -> s.setRevokedAt(now));
    }
}
