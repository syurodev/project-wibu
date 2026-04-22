package com.syuro.wibusystem.work.entity;

import com.syuro.wibusystem.shared.entity.BaseEntity;
import com.syuro.wibusystem.work.api.AgeRating;
import com.syuro.wibusystem.work.api.WorkStatus;
import com.syuro.wibusystem.work.api.WorkType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "works", schema = "catalog")
@SQLRestriction("deleted_at IS NULL")
public class Work extends BaseEntity {

    @Column(comment = "org từ identity schema — có giá trị khi work thuộc quyền sở hữu của org")
    private Long orgId;

    @Column(nullable = false, comment = "creator profile tạo work (luôn có, kể cả khi thuộc org)")
    private Long creatorProfileId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private WorkType type = WorkType.NOVEL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private WorkStatus status = WorkStatus.ON_GOING;

    @Column(nullable = false, length = 10, comment = "ngôn ngữ gốc khi tạo work")
    @Builder.Default
    private String originalLanguage = "jp";

    @Column(length = 500)
    private String cover;

    private LocalDate airedFrom;

    private LocalDate airedTo;

    @Enumerated(EnumType.STRING)
    @Column(length = 5)
    private AgeRating ageRating;

    @Builder.Default
    @Column(nullable = false)
    private boolean isOneshot = false;
}
