package com.syuro.wibusystem.security.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MagicLinkVerifyRequest {
    @NotBlank(message = "{VALIDATION.FIELD.REQUIRED}")
    private String token;
}
