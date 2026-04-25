package com.syuro.wibusystem.work.anime.repository;

import com.syuro.wibusystem.work.anime.entity.AnimeEpisode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimeEpisodeRepository extends JpaRepository<AnimeEpisode, Long> {
    long countByWorkId(Long workId);
}
