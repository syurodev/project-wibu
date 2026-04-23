package com.syuro.wibusystem.master_data.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class CreateOrganizationRequest {

    @NotEmpty
    private Map<String, String> names;

    private Map<String, String> biographies;

    @Size(max = 500)
    private String logo;

    @NotBlank
    @Size(max = 200)
    private String slug;
}
