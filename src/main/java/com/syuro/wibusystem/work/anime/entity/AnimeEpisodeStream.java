package com.syuro.wibusystem.work.anime.entity;

import com.syuro.wibusystem.shared.entity.BaseEntity;
import com.syuro.wibusystem.work.anime.api.StreamProvider;
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
    name = "anime_episode_streams",
    schema = "catalog",
    uniqueConstraints = @UniqueConstraint(columnNames = {"episode_id", "provider", "language"})
)
@SQLRestriction("deleted_at IS NULL")
public class AnimeEpisodeStream extends BaseEntity {

    @Column(nullable = false)
    private Long episodeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StreamProvider provider;

    @Column(nullable = false, length = 1000)
    private String url;

    @Column(nullable = false, length = 10, comment = "ngôn ngữ audio/sub: jp, en, vi")
    private String language;
}
