package com.syuro.wibusystem.work.dto;

import tools.jackson.databind.JsonNode;
import com.syuro.wibusystem.master_data.people.api.StaffRole;
import com.syuro.wibusystem.work.api.AgeRating;
import com.syuro.wibusystem.work.api.WorkStatus;
import com.syuro.wibusystem.work.api.WorkType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class CreateWorkRequest {

    @NotNull
    private WorkType type;

    @NotBlank
    @Size(max = 10)
    private String originalLanguage;

    @NotBlank
    @Size(max = 500)
    private String title;

    private WorkStatus status;

    private Long orgId;

    @Size(max = 500)
    private String cover;

    private LocalDate airedFrom;
    private LocalDate airedTo;

    private AgeRating ageRating;

    private boolean isOneshot = false;

    private JsonNode synopsis;

    private List<Long> genreIds;

    @Valid
    private List<StaffEntry> staffs;

    @Getter
    @Setter
    public static class StaffEntry {
        @NotNull
        private Long peopleId;

        @NotNull
        private StaffRole role;
    }
}
