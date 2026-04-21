package com.syuro.wibusystem.rbac.api;

import com.syuro.wibusystem.rbac.entity.GlobalPermission;
import com.syuro.wibusystem.rbac.entity.GlobalRole;
import com.syuro.wibusystem.rbac.entity.GlobalRolePermission;
import com.syuro.wibusystem.rbac.entity.UserGlobalRole;
import com.syuro.wibusystem.rbac.repository.GlobalPermissionRepository;
import com.syuro.wibusystem.rbac.repository.GlobalRolePermissionRepository;
import com.syuro.wibusystem.rbac.repository.GlobalRoleRepository;
import com.syuro.wibusystem.rbac.repository.UserGlobalRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Kiểm tra và lấy global permissions của một user.
 * Dùng bởi AuthService khi tạo JWT (nhét permissions vào token)
 * và bởi các module khi cần kiểm tra quyền phía server.
 */
@Service
@RequiredArgsConstructor
public class PermissionChecker {

    private final UserGlobalRoleRepository userGlobalRoleRepository;
    private final GlobalRoleRepository globalRoleRepository;
    private final GlobalRolePermissionRepository globalRolePermissionRepository;
    private final GlobalPermissionRepository globalPermissionRepository;

    /**
     * Trả về danh sách permissions đã expand wildcard của user.
     * Dùng khi tạo JWT — kết quả nhét vào claim "authorities".
     * <p>
     * Ví dụ: role "admin" có pattern "user:*" → expand thành ["user:view", "user:ban", "user:unban"]
     */
    @Transactional(readOnly = true)
    public List<String> getExpandedPermissions(Long userId) {
        List<Long> roleIds = userGlobalRoleRepository.findByUserId(userId)
                .stream().map(UserGlobalRole::getRoleId).toList();

        if (roleIds.isEmpty()) return List.of();

        List<String> patterns = globalRolePermissionRepository.findByRoleIdIn(roleIds)
                .stream().map(GlobalRolePermission::getPermission).toList();

        List<String> allPermissions = globalPermissionRepository.findAll()
                .stream().map(GlobalPermission::getName).toList();

        return expand(patterns, allPermissions);
    }

    @Transactional(readOnly = true)
    public List<String> getGlobalRoleNames(Long userId) {
        List<Long> roleIds = userGlobalRoleRepository.findByUserId(userId)
                .stream().map(UserGlobalRole::getRoleId).toList();
        if (roleIds.isEmpty()) return List.of();
        return globalRoleRepository.findAllById(roleIds)
                .stream().map(GlobalRole::getName).toList();
    }

    /**
     * Kiểm tra user có permission cụ thể không (server-side check).
     * Dùng khi không muốn phụ thuộc vào JWT claims (ví dụ: long-running jobs).
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(Long userId, String permission) {
        return getExpandedPermissions(userId).contains(permission);
    }

    /**
     * Expand danh sách patterns thành permissions cụ thể.
     * <p>
     * Rules:
     * "*"        → tất cả permissions
     * "user:*"   → tất cả permissions có category "user"
     * "user:ban" → chỉ permission đó
     */
    private List<String> expand(List<String> patterns, List<String> allPermissions) {
        if (patterns.contains("*")) return allPermissions;

        return patterns.stream()
                .flatMap(pattern -> {
                    if (pattern.endsWith(":*")) {
                        String prefix = pattern.substring(0, pattern.length() - 1); // "user:"
                        return allPermissions.stream().filter(p -> p.startsWith(prefix));
                    }
                    return java.util.stream.Stream.of(pattern);
                })
                .distinct()
                .toList();
    }
}
