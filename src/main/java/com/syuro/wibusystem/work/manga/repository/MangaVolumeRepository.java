package com.syuro.wibusystem.work.manga.repository;

import com.syuro.wibusystem.work.manga.entity.MangaVolume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MangaVolumeRepository extends JpaRepository<MangaVolume, Long> {
    long countByWorkId(Long workId);

    Optional<MangaVolume> findFirstByWorkIdOrderByVolumeNumberAsc(Long workId);
}
