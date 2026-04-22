package com.syuro.wibusystem.security.passkey.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasskeyRegisterBeginRequest {

    @Size(max = 100, message = "{VALIDATION.SIZE.MAX}")
    private String friendlyName;
}
