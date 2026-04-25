package com.syuro.wibusystem.master_data.organization.api;

import com.syuro.wibusystem.master_data.organization.entity.Organization;

import java.time.Instant;
import java.util.Map;

public record OrganizationResponse(
        String id,
        Map<String, String> names,
        Map<String, String> biographies,
        String logo,
        String slug,
        long viewCount,
        int totalWorks,
        Instant createdAt,
        Instant updatedAt
) {
    public static OrganizationResponse from(Organization org) {
        return new OrganizationResponse(
                org.getId().toString(),
                org.getNames(),
                org.getBiographies(),
                org.getLogo(),
                org.getSlug(),
                org.getViewCount(),
                org.getTotalWorks(),
                org.getCreatedAt(),
                org.getUpdatedAt()
        );
    }
}
