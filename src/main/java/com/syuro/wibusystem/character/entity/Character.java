package com.syuro.wibusystem.character.entity;

import com.syuro.wibusystem.character.api.CharacterGender;
import com.syuro.wibusystem.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "characters", schema = "catalog")
@SQLRestriction("deleted_at IS NULL")
public class Character extends BaseEntity {

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false, comment = "i18n: {jp: ナルト, en: Naruto, vi: Na-ru-to}")
    @Builder.Default
    private Map<String, String> names = new HashMap<>();

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(length = 500)
    private String avatar;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private CharacterGender gender;

    @Builder.Default
    @Column(name = "view_count", columnDefinition = "bigint default 0")
    private long viewCount = 0L;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false, comment = "i18n description")
    @Builder.Default
    private Map<String, String> biographies = new HashMap<>();
}
