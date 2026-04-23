package com.syuro.wibusystem.master_data.organization.api;

import java.time.Instant;
import java.util.Map;

public record OrganizationResponse(
        Long id,
        Map<String, String> names,
        Map<String, String> biographies,
        String logo,
        String slug,
        long viewCount,
        Instant createdAt,
        Instant updatedAt
) {}
