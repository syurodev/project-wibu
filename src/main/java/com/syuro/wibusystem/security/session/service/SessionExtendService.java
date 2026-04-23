package com.syuro.wibusystem.security.session.service;

import com.syuro.wibusystem.rbac.api.PermissionChecker;
import com.syuro.wibusystem.security.session.config.SessionProperties;
import com.syuro.wibusystem.security.session.dto.SessionCachePayload;
import com.syuro.wibusystem.security.session.entity.Session;
import com.syuro.wibusystem.security.session.repository.SessionRepository;
import com.syuro.wibusystem.user.api.UserQueryService;
import com.syuro.wibusystem.user.api.UserSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionExtendService {

    private final SessionProperties props;
    private final SessionRepository sessionRepository;
    private final UserQueryService userQueryService;
    private final PermissionChecker permissionChecker;

    /**
     * Sliding window: chỉ gia hạn expiresAt khi đã qua updateAge kể từ lần session được tạo/gia hạn.
     * Công thức: needsUpdateAt = (expiresAt - expiresIn) + updateAge
     */
    @Transactional
    public void extendIfNeeded(Session session) {
        long expiresIn = props.expiresIn();
        long updateAge = props.updateAge();

        long needsUpdateAt = session.getExpiresAt().toEpochMilli()
                - expiresIn * 1000
                + updateAge * 1000;

        if (Instant.now().toEpochMilli() >= needsUpdateAt) {
            session.setExpiresAt(Instant.now().plusSeconds(expiresIn));
            sessionRepository.save(session);
        }
    }

    /** Build SessionCachePayload từ Session entity sau khi DB lookup */
    public SessionCachePayload buildPayload(Session session) {
        UserSummary user = userQueryService.findById(session.getUserId());
        List<String> roles = permissionChecker.getGlobalRoleNames(user.id());
        List<String> permissions = permissionChecker.getExpandedPermissions(user.id());
        String version = String.valueOf(session.getUpdatedAt() != null
                ? session.getUpdatedAt().toEpochMilli()
                : session.getCreatedAt().toEpochMilli());

        return new SessionCachePayload(
                user.id(), user.email(), user.name(),
                roles, permissions, version, 0L
        );
    }
}
