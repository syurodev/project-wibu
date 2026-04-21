package com.syuro.wibusystem.rbac.api;

public enum OrgPermissionName {

    // org
    ORG_UPDATE("org:update", "org"),

    // member
    MEMBER_VIEW("member:view", "member"),
    MEMBER_INVITE("member:invite", "member"),
    MEMBER_REMOVE("member:remove", "member"),
    MEMBER_UPDATE("member:update", "member"),

    // request
    REQUEST_VIEW("request:view", "request"),
    REQUEST_CREATE("request:create", "request"),
    REQUEST_APPROVE("request:approve", "request"),
    REQUEST_REJECT("request:reject", "request"),

    // content (org-scoped)
    CONTENT_VIEW("content:view", "content"),
    CONTENT_CREATE("content:create", "content"),
    CONTENT_EDIT("content:edit", "content"),
    CONTENT_DELETE("content:delete", "content"),
    CONTENT_PUBLISH("content:publish", "content"),

    // translation
    TRANSLATION_VIEW("translation:view", "translation"),
    TRANSLATION_CREATE("translation:create", "translation"),
    TRANSLATION_EDIT("translation:edit", "translation"),
    TRANSLATION_DELETE("translation:delete", "translation"),
    TRANSLATION_PUBLISH("translation:publish", "translation");

    private final String value;
    private final String category;

    OrgPermissionName(String value, String category) {
        this.value = value;
        this.category = category;
    }

    public String value() {
        return value;
    }

    public String category() {
        return category;
    }
}
