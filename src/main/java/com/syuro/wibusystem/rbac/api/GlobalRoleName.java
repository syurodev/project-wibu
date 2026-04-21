package com.syuro.wibusystem.rbac.api;

public enum GlobalRoleName {
    USER("user"),
    CREATOR("creator"),
    MODERATOR("moderator"),
    ADMIN("admin"),
    SUPER_ADMIN("super_admin");

    private final String value;

    GlobalRoleName(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
