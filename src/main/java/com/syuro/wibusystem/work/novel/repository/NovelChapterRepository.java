package com.syuro.wibusystem.work.novel.repository;

import com.syuro.wibusystem.work.novel.entity.NovelChapter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NovelChapterRepository extends JpaRepository<NovelChapter, Long> {
    long countByWorkId(Long workId);
}
