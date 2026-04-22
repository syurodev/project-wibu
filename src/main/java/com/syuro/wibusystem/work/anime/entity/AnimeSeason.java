package com.syuro.wibusystem.work.anime.entity;

import com.syuro.wibusystem.shared.entity.BaseEntity;
import com.syuro.wibusystem.work.anime.api.AnimeCour;
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
    name = "anime_seasons",
    schema = "catalog",
    uniqueConstraints = @UniqueConstraint(columnNames = {"work_id", "season_number"})
)
@SQLRestriction("deleted_at IS NULL")
public class AnimeSeason extends BaseEntity {

    @Column(nullable = false)
    private Long workId;

    @Column(nullable = false)
    private int seasonNumber;

    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private AnimeCour cour;

    @Column(comment = "số tập dự kiến, null nếu chưa công bố")
    private Integer episodeCount;

    private LocalDate airedFrom;

    private LocalDate airedTo;

    @Column(length = 500)
    private String cover;

    @Builder.Default
    @Column(nullable = false)
    private boolean isVirtual = false;
}
