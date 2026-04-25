package com.syuro.wibusystem.creator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class RegisterCreatorRequest {

    @NotBlank
    @Size(max = 200)
    private String stageName;

    @NotBlank
    @Size(max = 200)
    private String slug;

    @Size(max = 500)
    private String avatar;

    @Size(max = 500)
    private String banner;

    private Map<String, String> bio;
}
