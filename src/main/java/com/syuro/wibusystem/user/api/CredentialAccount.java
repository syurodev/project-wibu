package com.syuro.wibusystem.user.api;

public record CredentialAccount(Long userId, String email, String passwordHash) {
}
