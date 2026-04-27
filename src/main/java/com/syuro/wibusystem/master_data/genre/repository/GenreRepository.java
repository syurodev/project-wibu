package com.syuro.wibusystem.master_data.genre.repository;

import com.syuro.wibusystem.master_data.genre.entity.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Long> {
    Optional<Genre> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Long id);

    @Query(
        value = """
            SELECT * FROM catalog.genres
            WHERE deleted_at IS NULL
              AND (titles::text ILIKE '%' || :q || '%' OR slug ILIKE '%' || :q || '%')
            """,
        countQuery = """
            SELECT count(*) FROM catalog.genres
            WHERE deleted_at IS NULL
              AND (titles::text ILIKE '%' || :q || '%' OR slug ILIKE '%' || :q || '%')
            """,
        nativeQuery = true
    )
    Page<Genre> search(@Param("q") String q, Pageable pageable);
}
