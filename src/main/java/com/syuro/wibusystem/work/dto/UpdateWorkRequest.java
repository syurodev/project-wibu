package com.syuro.wibusystem.work.dto;

import com.syuro.wibusystem.work.api.AgeRating;
import com.syuro.wibusystem.work.api.WorkStatus;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateWorkRequest {

    private WorkStatus status;

    @Size(max = 10)
    private String originalLanguage;

    @Size(max = 500)
    private String cover;

    private LocalDate airedFrom;
    private LocalDate airedTo;

    private AgeRating ageRating;
}
