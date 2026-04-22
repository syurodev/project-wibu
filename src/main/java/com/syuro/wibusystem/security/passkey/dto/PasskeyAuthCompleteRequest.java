package com.syuro.wibusystem.security.passkey.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.node.ObjectNode;

@Getter
@Setter
public class PasskeyAuthCompleteRequest {

    @NotBlank
    private String sessionKey;

    @NotNull
    private ObjectNode credential;
}
