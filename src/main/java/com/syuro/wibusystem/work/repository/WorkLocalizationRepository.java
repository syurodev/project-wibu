package com.syuro.wibusystem.work.repository;

import com.syuro.wibusystem.work.entity.WorkLocalization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkLocalizationRepository extends JpaRepository<WorkLocalization, Long> {
    boolean existsByWorkIdAndLanguage(Long workId, String language);
}
