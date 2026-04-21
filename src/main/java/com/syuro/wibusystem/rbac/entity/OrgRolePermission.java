package com.syuro.wibusystem.rbac.entity;

import com.syuro.wibusystem.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "org_role_permissions", schema = "identity",
        uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "permission"}))
public class OrgRolePermission extends BaseEntity {

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    // Lưu pattern — hỗ trợ wildcard: "*", "content:*", "translation:create"
    @Column(nullable = false, length = 100)
    private String permission;
}
