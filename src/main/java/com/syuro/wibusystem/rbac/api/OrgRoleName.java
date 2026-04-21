package com.syuro.wibusystem.rbac.api;

public enum OrgRoleName {
    OWNER("owner", 100),
    ADMIN("admin", 80),
    EDITOR("editor", 60),
    TRANSLATOR("translator", 40),
    MEMBER("member", 20);

    private final String value;
    private final int level;

    OrgRoleName(String value, int level) {
        this.value = value;
        this.level = level;
    }

    public String value() {
        return value;
    }

    public int level() {
        return level;
    }
}
