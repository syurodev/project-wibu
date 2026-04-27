package com.syuro.wibusystem.work.anime.repository;

import com.syuro.wibusystem.work.anime.entity.AnimeSeason;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnimeSeasonRepository extends JpaRepository<AnimeSeason, Long> {
    long countByWorkId(Long workId);

    Optional<AnimeSeason> findFirstByWorkIdOrderBySeasonNumberAsc(Long workId);
}
