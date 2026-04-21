package com.syuro.wibusystem.rbac.repository;

import com.syuro.wibusystem.rbac.entity.GlobalRolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GlobalRolePermissionRepository extends JpaRepository<GlobalRolePermission, Long> {
    List<GlobalRolePermission> findByRoleIdIn(List<Long> roleIds);
    boolean existsByRoleIdAndPermission(Long roleId, String permission);
}
