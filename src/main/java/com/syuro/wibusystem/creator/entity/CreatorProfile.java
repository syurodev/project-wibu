package com.syuro.wibusystem.creator.entity;

import com.syuro.wibusystem.creator.api.CreatorStatus;
import com.syuro.wibusystem.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "creator_profile", schema = "identity")
public class CreatorProfile extends BaseEntity {
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "stage_name", nullable = false)
    private String stageName;

    @Column(unique = true, nullable = false)
    private String slug; // dùng thay cho id trên web url

    private String avatar;
    private String banner;

    // Đa ngôn ngữ: {"vi": "...", "en": "..."}
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> bio;

    // ===== Status =====
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CreatorStatus status = CreatorStatus.ACTIVE;

    // ===== Stats (Denormalized) =====
    @Column(name = "novel_count", nullable = false)
    @Builder.Default
    private int novelCount = 0;

    @Column(name = "manga_count", nullable = false)
    @Builder.Default
    private int mangaCount = 0;

    @Column(name = "anime_count", nullable = false)
    @Builder.Default
    private int animeCount = 0;

    @Column(name = "translation_count", nullable = false)
    @Builder.Default
    private int translationCount = 0;

    @Column(name = "total_works", nullable = false)
    @Builder.Default
    private int totalWorks = 0;

    @Column(name = "total_views", nullable = false)
    @Builder.Default
    private long totalViews = 0L;

    @Column(name = "latest_work_at")
    private Instant latestWorkAt;
}
