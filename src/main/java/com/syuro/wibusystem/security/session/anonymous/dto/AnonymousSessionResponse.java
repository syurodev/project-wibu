package com.syuro.wibusystem.security.session.anonymous.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AnonymousSessionResponse(
        @JsonProperty("session_token") String sessionToken
) {}
