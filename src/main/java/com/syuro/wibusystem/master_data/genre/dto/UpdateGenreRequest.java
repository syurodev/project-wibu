package com.syuro.wibusystem.master_data.genre.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class UpdateGenreRequest {

    private Map<String, String> titles;

    @Size(max = 200)
    private String slug;
}
