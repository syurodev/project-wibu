package com.syuro.wibusystem.creator.repository;

import com.syuro.wibusystem.creator.entity.CreatorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CreatorProfileRepository extends JpaRepository<CreatorProfile, Long> {
    Optional<CreatorProfile> findByUserId(Long userId);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Long id);
}
