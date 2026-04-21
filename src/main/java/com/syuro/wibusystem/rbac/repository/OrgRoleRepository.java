package com.syuro.wibusystem.rbac.repository;

import com.syuro.wibusystem.rbac.entity.OrgRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrgRoleRepository extends JpaRepository<OrgRole, Long> {
    Optional<OrgRole> findByName(String name);
    boolean existsByName(String name);
}
