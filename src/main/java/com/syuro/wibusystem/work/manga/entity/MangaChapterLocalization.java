package com.syuro.wibusystem.work.manga.entity;

import com.syuro.wibusystem.shared.entity.BaseEntity;
import com.syuro.wibusystem.work.api.ChapterLocalizationStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(
    name = "manga_chapter_localizations",
    schema = "catalog",
    uniqueConstraints = @UniqueConstraint(columnNames = {"chapter_id", "language", "submitted_by"})
)
@SQLRestriction("deleted_at IS NULL")
public class MangaChapterLocalization extends BaseEntity {

    @Column(nullable = false)
    private Long chapterId;

    @Column(nullable = false, length = 10)
    private String language;

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
