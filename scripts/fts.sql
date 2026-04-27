-- 1) Bật extension (cần superuser; chỉ chạy 1 lần cho cả DB)
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- 2) Genres: index trên titles (jsonb) + slug
CREATE INDEX IF NOT EXISTS idx_genres_titles_trgm
    ON catalog.genres
    USING gin ((titles::text) gin_trgm_ops)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_genres_slug_trgm
    ON catalog.genres
    USING gin (slug gin_trgm_ops)
    WHERE deleted_at IS NULL;

-- 3) Peoples
CREATE INDEX IF NOT EXISTS idx_peoples_names_trgm
    ON catalog.peoples
    USING gin ((names::text) gin_trgm_ops)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_peoples_slug_trgm
    ON catalog.peoples
    USING gin (slug gin_trgm_ops)
    WHERE deleted_at IS NULL;

-- 4) Organizations
CREATE INDEX IF NOT EXISTS idx_organizations_names_trgm
    ON catalog.organizations
    USING gin ((names::text) gin_trgm_ops)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_organizations_slug_trgm
    ON catalog.organizations
    USING gin (slug gin_trgm_ops)
    WHERE deleted_at IS NULL;


-- Verify
EXPLAIN ANALYZE
SELECT * FROM catalog.genres
WHERE deleted_at IS NULL
  AND (titles::text ILIKE '%rom%' OR slug ILIKE '%rom%');