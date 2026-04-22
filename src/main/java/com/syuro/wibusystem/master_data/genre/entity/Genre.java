package com.syuro.wibusystem.master_data.genre.entity;

import com.syuro.wibusystem.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
@Table(name = "genres", schema = "catalog")
@SQLRestriction("deleted_at IS NULL")
public class Genre extends BaseEntity {
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false, comment = "lưu dạng đa ngôn ngữ ví dụ {en: Romance, vi: Tình cảm}")
    @Builder.Default
    private Map<String, String> titles = new HashMap<>();

    @Builder.Default
    @Column(name = "view_count", columnDefinition = "bigint default 0", comment = "đếm số view của thể loại")
    private long viewCount = 0L;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;
}
