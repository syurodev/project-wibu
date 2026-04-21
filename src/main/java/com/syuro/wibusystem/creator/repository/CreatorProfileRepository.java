package com.syuro.wibusystem.creator.repository;

import com.syuro.wibusystem.creator.entity.CreatorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreatorProfileRepository extends JpaRepository<CreatorProfile, Long> {
}
