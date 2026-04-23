package com.syuro.wibusystem.master_data.people.api;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

public record PeopleResponse(
        Long id,
        Map<String, String> names,
        Map<String, String> biographies,
        String avatar,
        LocalDate birthday,
        String slug,
        long viewCount,
        Instant createdAt,
        Instant updatedAt
) {}
