package com.syuro.wibusystem.work.repository;

import com.syuro.wibusystem.work.api.AgeRating;
import com.syuro.wibusystem.work.api.WorkStatus;
import com.syuro.wibusystem.work.api.WorkType;
import com.syuro.wibusystem.work.entity.Work;
import org.springframework.data.jpa.domain.Specification;

public class WorkSpec {

    public static Specification<Work> byType(WorkType type) {
        return (root, query, cb) ->
                type == null ? cb.conjunction() : cb.equal(root.get("type"), type);
    }

    public static Specification<Work> byStatus(WorkStatus status) {
        return (root, query, cb) ->
                status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Work> byAgeRating(AgeRating ageRating) {
        return (root, query, cb) ->
                ageRating == null ? cb.conjunction() : cb.equal(root.get("ageRating"), ageRating);
    }
}
