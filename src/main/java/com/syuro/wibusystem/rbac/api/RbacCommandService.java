package com.syuro.wibusystem.rbac.api;

import com.syuro.wibusystem.rbac.entity.OrgMembership;
import com.syuro.wibusystem.rbac.entity.UserGlobalRole;
import com.syuro.wibusystem.rbac.repository.GlobalRoleRepository;
import com.syuro.wibusystem.rbac.repository.OrgMembershipRepository;
import com.syuro.wibusystem.rbac.repository.OrgRoleRepository;
import com.syuro.wibusystem.rbac.repository.UserGlobalRoleRepository;
import com.syuro.wibusystem.shared.exception.AppException;
import com.syuro.wibusystem.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Quản lý việc gán / thu hồi roles.
 * Được gọi bởi các module khác (security khi đăng ký, creator khi tạo profile...).
 */
@Service
@RequiredArgsConstructor
public class RbacCommandService {

    private final GlobalRoleRepository globalRoleRepository;
    private final UserGlobalRoleRepository userGlobalRoleRepository;
    private final OrgRoleRepository orgRoleRepository;
    private final OrgMembershipRepository orgMembershipRepository;

    @Transactional
    public void assignGlobalRole(Long userId, String roleName) {
        Long roleId = globalRoleRepository.findByName(roleName)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND, roleName))
                .getId();

        if (!userGlobalRoleRepository.existsByUserIdAndRoleId(userId, roleId)) {
            userGlobalRoleRepository.save(
                    UserGlobalRole.builder().userId(userId).roleId(roleId).build()
            );
        }
    }

    @Transactional
    public void revokeGlobalRole(Long userId, String roleName) {
        Long roleId = globalRoleRepository.findByName(roleName)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND, roleName))
                .getId();

        userGlobalRoleRepository.deleteByUserIdAndRoleId(userId, roleId);
    }

    @Transactional
    public void assignOrgRole(Long userId, Long creatorProfileId, String roleName) {
        Long roleId = orgRoleRepository.findByName(roleName)
                .orElseThrow(() -> new AppException(ErrorCode.ORG_ROLE_NOT_FOUND, roleName))
                .getId();

        orgMembershipRepository.findByUserIdAndCreatorProfileId(userId, creatorProfileId)
                .ifPresentOrElse(
                        m -> m.setRoleId(roleId),
                        () -> orgMembershipRepository.save(OrgMembership.builder()
                                .userId(userId)
                                .creatorProfileId(creatorProfileId)
                                .roleId(roleId)
                                .build())
                );
    }

    @Transactional
    public void revokeOrgRole(Long userId, Long creatorProfileId) {
        orgMembershipRepository.findByUserIdAndCreatorProfileId(userId, creatorProfileId)
                .ifPresent(orgMembershipRepository::delete);
    }
}
