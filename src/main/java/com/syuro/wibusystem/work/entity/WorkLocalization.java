package com.syuro.wibusystem.work.entity;

import com.syuro.wibusystem.shared.entity.BaseEntity;
import com.syuro.wibusystem.work.api.WorkLocalizationStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(
        name = "work_localizations",
        schema = "catalog",
        uniqueConstraints = @UniqueConstraint(columnNames = {"work_id", "language"})
)
@SQLRestriction("deleted_at IS NULL")
public class WorkLocalization extends BaseEntity {

    @Column(nullable = false)
    private Long workId;

    @Column(nullable = false, length = 10)
    private String language;

    @Column(length = 500)
    private String title;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", comment = "rich text synopsis từ plate editor dạng JSON string")
    private String synopsis;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, comment = "APPROVED tự động nếu là localization của original_language")
    @Builder.Default
    private WorkLocalizationStatus status = WorkLocalizationStatus.PENDING;

    @Column(comment = "creatorProfileId của người submit")
    private Long submittedBy;

    @Column(comment = "người duyệt")
    private Long approvedBy;

    @Column(comment = "thời gian duyệt")
    private Instant approvedAt;

    @Column(comment = "người từ chối")
    private Long rejectedBy;

    @Column(comment = "thời gian từ chối")
    private Instant rejectedAt;

    @Column(length = 1000, comment = "lý do từ chối")
    private String rejectionReason;

    @Column(nullable = false, comment = "Đánh dấu đây là bản gốc")
    private boolean isOriginal = false;
}
