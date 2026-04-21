package com.syuro.wibusystem.rbac.repository;

import com.syuro.wibusystem.rbac.entity.UserGlobalRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserGlobalRoleRepository extends JpaRepository<UserGlobalRole, Long> {
    List<UserGlobalRole> findByUserId(Long userId);
    boolean existsByUserIdAndRoleId(Long userId, Long roleId);
    void deleteByUserIdAndRoleId(Long userId, Long roleId);
}
