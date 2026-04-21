package com.syuro.wibusystem.rbac.api;

import com.syuro.wibusystem.rbac.entity.OrgMembership;
import com.syuro.wibusystem.rbac.repository.OrgMembershipRepository;
import com.syuro.wibusystem.rbac.repository.OrgPermissionRepository;
import com.syuro.wibusystem.rbac.repository.OrgRolePermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Kiểm tra org-level permissions của một user trong một organization (CreatorProfile).
 */
@Service
@RequiredArgsConstructor
public class OrgPermissionChecker {

    private final OrgMembershipRepository orgMembershipRepository;
    private final OrgRolePermissionRepository orgRolePermissionRepository;
    private final OrgPermissionRepository orgPermissionRepository;

    @Transactional(readOnly = true)
    public boolean hasOrgPermission(Long userId, Long creatorProfileId, String permission) {
        return getExpandedOrgPermissions(userId, creatorProfileId).contains(permission);
    }

    @Transactional(readOnly = true)
    public List<String> getExpandedOrgPermissions(Long userId, Long creatorProfileId) {
        return orgMembershipRepository.findByUserIdAndCreatorProfileId(userId, creatorProfileId)
                .map(membership -> {
                    List<String> patterns = orgRolePermissionRepository
                            .findByRoleId(membership.getRoleId())
                            .stream().map(p -> p.getPermission()).toList();

                    List<String> allPermissions = orgPermissionRepository.findAll()
                            .stream().map(p -> p.getName()).toList();

                    return expand(patterns, allPermissions);
                })
                .orElse(List.of());
    }

    private List<String> expand(List<String> patterns, List<String> allPermissions) {
        if (patterns.contains("*")) return allPermissions;

        return patterns.stream()
                .flatMap(pattern -> {
                    if (pattern.endsWith(":*")) {
                        String prefix = pattern.substring(0, pattern.length() - 1);
                        return allPermissions.stream().filter(p -> p.startsWith(prefix));
                    }
                    return java.util.stream.Stream.of(pattern);
                })
                .distinct()
                .toList();
    }
}
