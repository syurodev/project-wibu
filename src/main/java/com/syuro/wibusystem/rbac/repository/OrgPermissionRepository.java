package com.syuro.wibusystem.rbac.repository;

import com.syuro.wibusystem.rbac.entity.OrgPermission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrgPermissionRepository extends JpaRepository<OrgPermission, Long> {
    boolean existsByName(String name);
}
