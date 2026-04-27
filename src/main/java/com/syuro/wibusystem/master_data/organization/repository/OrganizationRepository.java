package com.syuro.wibusystem.master_data.organization.repository;

import com.syuro.wibusystem.master_data.organization.entity.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Long id);

    @Query(
        value = """
            SELECT * FROM catalog.organizations
            WHERE deleted_at IS NULL
              AND (names::text ILIKE '%' || :q || '%' OR slug ILIKE '%' || :q || '%')
            """,
        countQuery = """
            SELECT count(*) FROM catalog.organizations
            WHERE deleted_at IS NULL
              AND (names::text ILIKE '%' || :q || '%' OR slug ILIKE '%' || :q || '%')
            """,
        nativeQuery = true
    )
    Page<Organization> search(@Param("q") String q, Pageable pageable);
}
