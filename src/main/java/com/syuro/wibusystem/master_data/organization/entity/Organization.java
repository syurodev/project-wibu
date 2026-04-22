package com.syuro.wibusystem.master_data.organization.entity;

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
@Table(name = "organizations", schema = "catalog")
@SQLRestriction("deleted_at IS NULL")
public class Organization extends BaseEntity {
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false, comment = "lưu dạng đa ngôn ngữ ví dụ { jp: 尾田 栄一郎, en: Eiichiro Oda }")
    @Builder.Default
    private Map<String, String> names = new HashMap<>();

    @Builder.Default
    @Column(name = "view_count", columnDefinition = "bigint default 0", comment = "đếm số view")
    private long viewCount = 0L;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(length = 500)
    private String logo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false, comment = "lưu dạng đa ngôn ngữ như title")
    @Builder.Default
    private Map<String, String> biographies = new HashMap<>();
}
