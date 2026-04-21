package com.syuro.wibusystem.rbac.repository;

import com.syuro.wibusystem.rbac.entity.GlobalPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GlobalPermissionRepository extends JpaRepository<GlobalPermission, Long> {
    List<GlobalPermission> findByCategory(String category);
    boolean existsByName(String name);
}
