package com.syuro.wibusystem.work.manga.entity;

import com.syuro.wibusystem.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(
    name = "manga_chapters",
    schema = "catalog",
    uniqueConstraints = @UniqueConstraint(columnNames = {"work_id", "chapter_number"})
)
@SQLRestriction("deleted_at IS NULL")
public class MangaChapter extends BaseEntity {

    @Column(nullable = false)
    private Long workId;

    private Long volumeId;

    @Column(nullable = false, columnDefinition = "real")
    private float chapterNumber;

    @Column(length = 500)
    private String title;

    @Builder.Default
    @Column(nullable = false)
    private boolean isExtra = false;

    private LocalDate publishedAt;

    @Builder.Default
    @Column(nullable = false)
    private long viewCount = 0L;
}
