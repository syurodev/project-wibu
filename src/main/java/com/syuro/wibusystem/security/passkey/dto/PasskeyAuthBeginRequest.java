package com.syuro.wibusystem.security.passkey.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasskeyAuthBeginRequest {

    @Email(message = "{VALIDATION.EMAIL.INVALID}")
    private String email;
}
