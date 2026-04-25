package com.syuro.wibusystem.work.manga.repository;

import com.syuro.wibusystem.work.manga.entity.MangaChapter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MangaChapterRepository extends JpaRepository<MangaChapter, Long> {
    long countByWorkId(Long workId);
}
