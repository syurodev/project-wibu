package com.syuro.wibusystem.rbac.seed;

import com.syuro.wibusystem.rbac.api.GlobalPermissionName;
import com.syuro.wibusystem.rbac.api.GlobalRoleName;
import com.syuro.wibusystem.rbac.api.OrgPermissionName;
import com.syuro.wibusystem.rbac.api.OrgRoleName;
import com.syuro.wibusystem.rbac.entity.GlobalPermission;
import com.syuro.wibusystem.rbac.entity.GlobalRole;
import com.syuro.wibusystem.rbac.entity.GlobalRolePermission;
import com.syuro.wibusystem.rbac.entity.OrgPermission;
import com.syuro.wibusystem.rbac.entity.OrgRole;
import com.syuro.wibusystem.rbac.entity.OrgRolePermission;
import com.syuro.wibusystem.rbac.repository.GlobalPermissionRepository;
import com.syuro.wibusystem.rbac.repository.GlobalRolePermissionRepository;
import com.syuro.wibusystem.rbac.repository.GlobalRoleRepository;
import com.syuro.wibusystem.rbac.repository.OrgPermissionRepository;
import com.syuro.wibusystem.rbac.repository.OrgRolePermissionRepository;
import com.syuro.wibusystem.rbac.repository.OrgRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RbacSeeder implements ApplicationRunner {

    private final GlobalRoleRepository globalRoleRepository;
    private final GlobalPermissionRepository globalPermissionRepository;
    private final GlobalRolePermissionRepository globalRolePermissionRepository;
    private final OrgRoleRepository orgRoleRepository;
    private final OrgPermissionRepository orgPermissionRepository;
    private final OrgRolePermissionRepository orgRolePermissionRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedGlobalPermissions();
        seedGlobalRoles();
        seedGlobalRolePermissions();
        seedOrgPermissions();
        seedOrgRoles();
        seedOrgRolePermissions();
        log.info("RBAC seed completed");
    }

    // ===================== GLOBAL =====================

    private void seedGlobalPermissions() {
        for (GlobalPermissionName p : GlobalPermissionName.values()) {
            if (!globalPermissionRepository.existsByName(p.value())) {
                globalPermissionRepository.save(
                        GlobalPermission.builder().name(p.value()).category(p.category()).build()
                );
            }
        }
    }

    private void seedGlobalRoles() {
        for (GlobalRoleName r : GlobalRoleName.values()) {
            if (!globalRoleRepository.existsByName(r.value())) {
                globalRoleRepository.save(
                        GlobalRole.builder().name(r.value()).system(true).build()
                );
            }
        }
    }

    private void seedGlobalRolePermissions() {
        // Patterns theo rbac.md — wildcard được expand lúc runtime bởi PermissionChecker
        Map<GlobalRoleName, List<String>> matrix = Map.of(
                GlobalRoleName.SUPER_ADMIN, List.of("*"),
                GlobalRoleName.ADMIN,       List.of("user:*", "creator:*", "content:*", "comment:*", "profile:*"),
                GlobalRoleName.MODERATOR,   List.of("content:view_reports", "content:moderate",
                                                     "content:delete_any", "comment:delete_any"),
                GlobalRoleName.CREATOR,     List.of("content:create", "content:edit_own", "content:delete_own",
                                                     "content:publish_own", "content:report", "comment:*",
                                                     "profile:edit_own", "bookmark:manage", "follow:manage",
                                                     "rating:manage"),
                GlobalRoleName.USER,        List.of("comment:create", "comment:edit_own", "comment:delete_own",
                                                     "comment:report", "profile:edit_own", "bookmark:manage",
                                                     "follow:manage", "rating:manage", "content:report")
        );

        matrix.forEach((roleName, patterns) -> {
            Long roleId = globalRoleRepository.findByName(roleName.value())
                    .orElseThrow().getId();

            for (String pattern : patterns) {
                if (!globalRolePermissionRepository.existsByRoleIdAndPermission(roleId, pattern)) {
                    globalRolePermissionRepository.save(
                            GlobalRolePermission.builder().roleId(roleId).permission(pattern).build()
                    );
                }
            }
        });
    }

    // ===================== ORG =====================

    private void seedOrgPermissions() {
        for (OrgPermissionName p : OrgPermissionName.values()) {
            if (!orgPermissionRepository.existsByName(p.value())) {
                orgPermissionRepository.save(
                        OrgPermission.builder().name(p.value()).category(p.category()).build()
                );
            }
        }
    }

    private void seedOrgRoles() {
        for (OrgRoleName r : OrgRoleName.values()) {
            if (!orgRoleRepository.existsByName(r.value())) {
                orgRoleRepository.save(
                        OrgRole.builder().name(r.value()).level(r.level()).system(true).build()
                );
            }
        }
    }

    private void seedOrgRolePermissions() {
        Map<OrgRoleName, List<String>> matrix = Map.of(
                OrgRoleName.OWNER,      List.of("*"),
                OrgRoleName.ADMIN,      List.of("org:update", "member:*", "request:*", "content:*", "translation:*"),
                OrgRoleName.EDITOR,     List.of("content:*", "translation:*"),
                OrgRoleName.TRANSLATOR, List.of("translation:create", "translation:edit"),
                OrgRoleName.MEMBER,     List.of("member:view")
        );

        matrix.forEach((roleName, patterns) -> {
            Long roleId = orgRoleRepository.findByName(roleName.value())
                    .orElseThrow().getId();

            for (String pattern : patterns) {
                if (!orgRolePermissionRepository.existsByRoleIdAndPermission(roleId, pattern)) {
                    orgRolePermissionRepository.save(
                            OrgRolePermission.builder().roleId(roleId).permission(pattern).build()
                    );
                }
            }
        });
    }
}
