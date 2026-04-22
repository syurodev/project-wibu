package com.syuro.wibusystem.work.manga.entity;

import com.syuro.wibusystem.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(
    name = "manga_chapter_pages",
    schema = "catalog",
    uniqueConstraints = @UniqueConstraint(columnNames = {"chapter_id", "page_number"})
)
@SQLRestriction("deleted_at IS NULL")
public class MangaChapterPage extends BaseEntity {

    @Column(nullable = false)
    private Long chapterId;

    @Column(nullable = false)
    private int pageNumber;

    @Column(nullable = false, length = 1000)
    private String imageUrl;
}
