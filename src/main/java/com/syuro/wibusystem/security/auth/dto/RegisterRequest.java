package com.syuro.wibusystem.security.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    @NotBlank(message = "{VALIDATION.FIELD.REQUIRED}")
    @Email(message = "{VALIDATION.EMAIL.INVALID}")
    private String email;

    @NotBlank(message = "{VALIDATION.FIELD.REQUIRED}")
    @Size(min = 8, message = "{VALIDATION.PASSWORD.MIN_LENGTH}")
    private String password;

    @NotBlank(message = "{VALIDATION.FIELD.REQUIRED}")
    private String name;
}
