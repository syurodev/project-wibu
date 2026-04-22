package com.syuro.wibusystem.work.entity;

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
    name = "work_genres",
    schema = "catalog",
    uniqueConstraints = @UniqueConstraint(columnNames = {"work_id", "genre_id"})
)
@SQLRestriction("deleted_at IS NULL")
public class WorkGenre extends BaseEntity {

    @Column(nullable = false)
    private Long workId;

    @Column(nullable = false)
    private Long genreId;
}
