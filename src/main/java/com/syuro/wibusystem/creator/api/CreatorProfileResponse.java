package com.syuro.wibusystem.creator.api;

import com.syuro.wibusystem.creator.entity.CreatorProfile;

import java.time.Instant;
import java.util.Map;

public record CreatorProfileResponse(
        String id,
        Long userId,
        String stageName,
        String slug,
        String avatar,
        String banner,
        Map<String, String> bio,
        CreatorStatus status,
        int novelCount,
        int mangaCount,
        int animeCount,
        int translationCount,
        int totalWorks,
        long totalViews,
        Instant latestWorkAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static CreatorProfileResponse from(CreatorProfile p) {
        return new CreatorProfileResponse(
                p.getId().toString(),
                p.getUserId(),
                p.getStageName(),
                p.getSlug(),
                p.getAvatar(),
                p.getBanner(),
                p.getBio(),
                p.getStatus(),
                p.getNovelCount(),
                p.getMangaCount(),
                p.getAnimeCount(),
                p.getTranslationCount(),
                p.getTotalWorks(),
                p.getTotalViews(),
                p.getLatestWorkAt(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
