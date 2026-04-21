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
@Table(name = "org_roles", schema = "identity",
        uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class OrgRole extends BaseEntity {

    // owner, admin, editor, translator, member
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    // 100=owner, 80=admin, 60=editor, 40=translator, 20=member
    @Column(nullable = false)
    private int level;

    @Column(name = "is_system", nullable = false)
    private boolean system = false;
}
