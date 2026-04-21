package com.syuro.wibusystem.rbac.repository;

import com.syuro.wibusystem.rbac.entity.OrgRolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrgRolePermissionRepository extends JpaRepository<OrgRolePermission, Long> {
    List<OrgRolePermission> findByRoleId(Long roleId);
    boolean existsByRoleIdAndPermission(Long roleId, String permission);
}
