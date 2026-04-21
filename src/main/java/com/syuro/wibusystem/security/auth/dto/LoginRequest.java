package com.syuro.wibusystem.security.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    /** Email hoặc username */
    @NotBlank(message = "{VALIDATION.FIELD.REQUIRED}")
    private String identifier;

    @NotBlank(message = "{VALIDATION.FIELD.REQUIRED}")
    @Size(min = 8, message = "{VALIDATION.PASSWORD.MIN_LENGTH}")
    private String password;
}
