package com.syuro.wibusystem.work.novel.entity;

import com.syuro.wibusystem.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(
        name = "novel_volumes",
        schema = "catalog",
        uniqueConstraints = @UniqueConstraint(columnNames = {"work_id", "volume_number"})
)
@SQLRestriction("deleted_at IS NULL")
public class NovelVolume extends BaseEntity {

    @Column(nullable = false)
    private Long workId;

    @Column(nullable = false)
    private int volumeNumber;

    @Column(length = 500)
    private String cover;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", comment = "lưu đa ngôn ngữ dạng {vi: ..., en: ...}")
    private Map<String, String> titles;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", comment = "lưu đa ngôn ngữ dạng {vi: ..., en: ...}")
    @Builder.Default
    private Map<String, String> synopsis = new HashMap<>();

    private LocalDate publishedAt;

    @Builder.Default
    @Column(nullable = false)
    private boolean isVirtual = false;
}
