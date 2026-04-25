package com.syuro.wibusystem.work.api;

import com.syuro.wibusystem.work.entity.Work;

import java.time.Instant;
import java.time.LocalDate;

public record WorkResponse(
        String id,
        Long orgId,
        Long creatorProfileId,
        WorkType type,
        WorkStatus status,
        String originalLanguage,
        String cover,
        LocalDate airedFrom,
        LocalDate airedTo,
        AgeRating ageRating,
        boolean isOneshot,
        Instant createdAt,
        Instant updatedAt
) {
    public static WorkResponse from(Work work) {
        return new WorkResponse(
                work.getId().toString(),
                work.getOrgId(),
                work.getCreatorProfileId(),
                work.getType(),
                work.getStatus(),
                work.getOriginalLanguage(),
                work.getCover(),
                work.getAiredFrom(),
                work.getAiredTo(),
                work.getAgeRating(),
                work.isOneshot(),
                work.getCreatedAt(),
                work.getUpdatedAt()
        );
    }
}
