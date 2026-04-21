package com.syuro.wibusystem.user.entity;

import com.syuro.wibusystem.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users", schema = "identity")
public class User extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @Column(name = "another_name", nullable = true) // biệt danh
    private String anotherName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = true) // check unique ở app vì khi đăng ký user chỉ có email
    private String username;

    private String avatar;

    private String cover;

    @Column(name = "is_anonymous", nullable = false)
    private boolean isAnonymous = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "social_links", columnDefinition = "jsonb")
    private Map<String, String> socialLinks; // {facebook, instagram, ...}

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private Map<String, Object> settings = new HashMap<>();

}
