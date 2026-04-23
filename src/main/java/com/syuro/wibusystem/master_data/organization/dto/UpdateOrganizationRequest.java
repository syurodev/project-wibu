package com.syuro.wibusystem.master_data.organization.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class UpdateOrganizationRequest {

    private Map<String, String> names;

    private Map<String, String> biographies;

    @Size(max = 500)
    private String logo;

    @Size(max = 200)
    private String slug;
}
