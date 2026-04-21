package com.syuro.wibusystem.security.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpRequest {
    @NotBlank(message = "{VALIDATION.FIELD.REQUIRED}")
    @JsonProperty("user_id")
    private String userId;

    @NotBlank(message = "{VALIDATION.FIELD.REQUIRED}")
    @Pattern(regexp = "\\d{6}", message = "{VALIDATION.OTP.INVALID_FORMAT}")
    private String otp;
}
