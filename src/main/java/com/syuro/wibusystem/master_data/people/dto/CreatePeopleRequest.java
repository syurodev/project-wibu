package com.syuro.wibusystem.master_data.people.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
public class CreatePeopleRequest {

    @NotEmpty
    private Map<String, String> names;

    private Map<String, String> biographies;

    @Size(max = 500)
    private String avatar;

    private LocalDate birthday;

    @NotBlank
    @Size(max = 200)
    private String slug;
}
