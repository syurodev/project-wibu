package com.syuro.wibusystem.master_data.genre.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class CreateGenreRequest {

    @NotEmpty
    private Map<String, String> titles;

    @NotBlank
    @Size(max = 200)
    private String slug;
}
