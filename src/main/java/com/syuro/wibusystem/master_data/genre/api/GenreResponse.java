package com.syuro.wibusystem.master_data.genre.api;

import com.syuro.wibusystem.master_data.genre.entity.Genre;

import java.time.Instant;
import java.util.Map;

public record GenreResponse(
        String id,
        Map<String, String> titles,
        String slug,
        long viewCount,
        int totalWorks,
        Instant createdAt,
        Instant updatedAt
) {
    public static GenreResponse from(Genre genre) {
        return new GenreResponse(
                genre.getId().toString(),
                genre.getTitles(),
                genre.getSlug(),
                genre.getViewCount(),
                genre.getTotalWorks(),
                genre.getCreatedAt(),
                genre.getUpdatedAt()
        );
    }
}
