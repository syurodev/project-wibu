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
@Table(name = "org_memberships", schema = "identity",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "creator_profile_id"}))
public class OrgMembership extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "creator_profile_id", nullable = false)
    private Long creatorProfileId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;
}
