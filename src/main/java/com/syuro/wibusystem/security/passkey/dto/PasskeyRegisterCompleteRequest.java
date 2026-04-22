package com.syuro.wibusystem.security.passkey.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.node.ObjectNode;

@Getter
@Setter
public class PasskeyRegisterCompleteRequest {

    @NotNull
    private ObjectNode credential;
}
