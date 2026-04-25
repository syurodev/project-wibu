package com.syuro.wibusystem.work.dto;

import com.syuro.wibusystem.work.api.AgeRating;
import com.syuro.wibusystem.work.api.WorkStatus;
import com.syuro.wibusystem.work.api.WorkType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateWorkRequest {

    @NotNull
    private WorkType type;

    @NotBlank
    @Size(max = 10)
    private String originalLanguage;

    private WorkStatus status;

    private Long orgId;

    @Size(max = 500)
    private String cover;

    private LocalDate airedFrom;
    private LocalDate airedTo;

    private AgeRating ageRating;

    private boolean isOneshot = false;
}
