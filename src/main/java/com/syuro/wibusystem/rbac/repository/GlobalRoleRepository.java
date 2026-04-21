package com.syuro.wibusystem.rbac.repository;

import com.syuro.wibusystem.rbac.entity.GlobalRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GlobalRoleRepository extends JpaRepository<GlobalRole, Long> {
    Optional<GlobalRole> findByName(String name);
    boolean existsByName(String name);
}
