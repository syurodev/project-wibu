package com.syuro.wibusystem.master_data.people.api;

import com.syuro.wibusystem.master_data.people.entity.People;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

public record PeopleResponse(
        String id,
        Map<String, String> names,
        Map<String, String> biographies,
        String avatar,
        LocalDate birthday,
        String slug,
        long viewCount,
        int totalWorks,
        Instant createdAt,
        Instant updatedAt
) {
    public static PeopleResponse from(People people) {
        return new PeopleResponse(
                people.getId().toString(),
                people.getNames(),
                people.getBiographies(),
                people.getAvatar(),
                people.getBirthday(),
                people.getSlug(),
                people.getViewCount(),
                people.getTotalWorks(),
                people.getCreatedAt(),
                people.getUpdatedAt()
        );
    }
}
