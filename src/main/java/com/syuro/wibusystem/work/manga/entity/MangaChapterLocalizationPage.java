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
    name = "manga_chapter_localization_pages",
    schema = "catalog",
    uniqueConstraints = @UniqueConstraint(columnNames = {"localization_id", "page_number"})
)
@SQLRestriction("deleted_at IS NULL")
public class MangaChapterLocalizationPage extends BaseEntity {

    @Column(nullable = false)
    private Long localizationId;

    @Column(nullable = false)
    private int pageNumber;

    @Column(nullable = false, length = 1000, comment = "trang đã dịch/typeset")
    private String imageUrl;
}
