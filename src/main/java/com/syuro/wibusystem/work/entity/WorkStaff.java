package com.syuro.wibusystem.work.entity;

import com.syuro.wibusystem.master_data.people.api.StaffRole;
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
    name = "work_staffs",
    schema = "catalog",
    uniqueConstraints = @UniqueConstraint(columnNames = {"work_id", "people_id", "role"})
)
@SQLRestriction("deleted_at IS NULL")
public class WorkStaff extends BaseEntity {

    @Column(nullable = false)
    private Long workId;

    @Column(nullable = false)
    private Long peopleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StaffRole role;
}
