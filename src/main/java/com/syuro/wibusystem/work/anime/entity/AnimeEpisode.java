package com.syuro.wibusystem.work.anime.entity;

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
    name = "anime_episodes",
    schema = "catalog",
    uniqueConstraints = @UniqueConstraint(columnNames = {"season_id", "episode_number"})
)
@SQLRestriction("deleted_at IS NULL")
public class AnimeEpisode extends BaseEntity {

    @Column(nullable = false, comment = "denormalized — tránh JOIN qua season khi query toàn bộ tập")
    private Long workId;

    @Column(nullable = false)
    private Long seasonId;

    @Column(nullable = false, columnDefinition = "real")
    private float episodeNumber;

    @Column(length = 500)
    private String title;

    @Column(comment = "thời lượng tính bằng giây")
    private Integer durationSeconds;

    @Column(length = 500)
    private String thumbnail;

    @Builder.Default
    @Column(nullable = false)
    private boolean isRecap = false;

    private LocalDate airedAt;

    @Builder.Default
    @Column(nullable = false)
    private long viewCount = 0L;
}
