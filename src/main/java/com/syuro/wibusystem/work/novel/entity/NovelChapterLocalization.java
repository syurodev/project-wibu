package com.syuro.wibusystem.work.novel.entity;

import com.syuro.wibusystem.shared.entity.BaseEntity;
import com.syuro.wibusystem.work.api.ChapterLocalizationStatus;
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
    name = "novel_chapter_localizations",
    schema = "catalog",
    uniqueConstraints = @UniqueConstraint(columnNames = {"chapter_id", "language", "submitted_by"})
)
@SQLRestriction("deleted_at IS NULL")
public class NovelChapterLocalization extends BaseEntity {

    @Column(nullable = false)
    private Long chapterId;

    @Column(nullable = false, length = 10)
    private String language;

    @Column(length = 500, comment = "tiêu đề chapter đã dịch")
    private String title;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", comment = "rich text từ plate editor dạng JSON string")
    private String content;

    private Integer wordCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ChapterLocalizationStatus status = ChapterLocalizationStatus.DRAFT;

    @Column(nullable = false, comment = "creatorProfileId của người submit")
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
}
