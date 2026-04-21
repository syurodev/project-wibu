package com.syuro.wibusystem.rbac.repository;

import com.syuro.wibusystem.rbac.entity.OrgMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrgMembershipRepository extends JpaRepository<OrgMembership, Long> {
    Optional<OrgMembership> findByUserIdAndCreatorProfileId(Long userId, Long creatorProfileId);
    boolean existsByUserIdAndCreatorProfileId(Long userId, Long creatorProfileId);
}
