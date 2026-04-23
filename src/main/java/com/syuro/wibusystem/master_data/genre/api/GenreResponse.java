package com.syuro.wibusystem.master_data.genre.api;

import java.time.Instant;
import java.util.Map;

public record GenreResponse(
        Long id,
        Map<String, String> titles,
        String slug,
        long viewCount,
        Instant createdAt,
        Instant updatedAt
) {}
