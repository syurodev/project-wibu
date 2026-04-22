package com.syuro.wibusystem.security.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
public class MagicLinkSendRequest {
    @NotBlank(message = "{VALIDATION.FIELD.REQUIRED}")
    @Email(message = "{VALIDATION.EMAIL.INVALID}")
    private String email;

    @NotBlank(message = "{VALIDATION.FIELD.REQUIRED}")
    @URL(message = "{VALIDATION.URL.INVALID}")
    private String callbackUrl;
}
