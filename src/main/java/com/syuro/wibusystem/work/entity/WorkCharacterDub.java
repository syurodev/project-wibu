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
    name = "work_character_dubs",
    schema = "catalog",
    uniqueConstraints = @UniqueConstraint(columnNames = {"work_character_id", "voice_actor_id"})
)
@SQLRestriction("deleted_at IS NULL")
public class WorkCharacterDub extends BaseEntity {

    @Column(nullable = false)
    private Long workCharacterId;

    @Column(nullable = false, comment = "peopleId của seiyuu")
    private Long voiceActorId;

    @Column(length = 10, comment = "ngôn ngữ bản lồng tiếng: jp, en, vi")
    private String language;
}
