package com.syuro.wibusystem.work.entity;

import com.syuro.wibusystem.shared.entity.BaseEntity;
import com.syuro.wibusystem.work.api.OrgRole;
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
    name = "work_organizations",
    schema = "catalog",
    uniqueConstraints = @UniqueConstraint(columnNames = {"work_id", "org_id", "role"})
)
@SQLRestriction("deleted_at IS NULL")
public class WorkOrganization extends BaseEntity {

    @Column(nullable = false)
    private Long workId;

    @Column(nullable = false)
    private Long orgId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private OrgRole role;
}
