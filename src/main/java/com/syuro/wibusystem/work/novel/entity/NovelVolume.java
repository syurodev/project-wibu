package com.syuro.wibusystem.work.novel.entity;

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

    private LocalDate publishedAt;

    @Builder.Default
    @Column(nullable = false)
    private boolean isVirtual = false;
}
