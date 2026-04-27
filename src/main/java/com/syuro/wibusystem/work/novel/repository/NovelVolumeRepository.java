package com.syuro.wibusystem.work.novel.repository;

import com.syuro.wibusystem.work.novel.entity.NovelVolume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NovelVolumeRepository extends JpaRepository<NovelVolume, Long> {
    long countByWorkId(Long workId);

    Optional<NovelVolume> findFirstByWorkIdOrderByVolumeNumberAsc(Long workId);
}
