package com.syuro.wibusystem.security.passkey.dto;

import tools.jackson.databind.JsonNode;

public record PasskeyAuthBeginResponse(String sessionKey, JsonNode options) {}
