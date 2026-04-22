package com.syuro.wibusystem.work.entity;

import com.syuro.wibusystem.shared.entity.BaseEntity;
import com.syuro.wibusystem.work.api.CharacterRole;
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
    name = "work_characters",
    schema = "catalog",
    uniqueConstraints = @UniqueConstraint(columnNames = {"work_id", "character_id"})
)
@SQLRestriction("deleted_at IS NULL")
public class WorkCharacter extends BaseEntity {

    @Column(nullable = false)
    private Long workId;

    @Column(nullable = false)
    private Long characterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private CharacterRole role;
}
