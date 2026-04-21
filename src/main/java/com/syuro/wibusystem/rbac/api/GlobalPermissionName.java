package com.syuro.wibusystem.rbac.api;

public enum GlobalPermissionName {

    // profile
    PROFILE_EDIT_OWN("profile:edit_own", "profile"),

    // comment
    COMMENT_CREATE("comment:create", "comment"),
    COMMENT_EDIT_OWN("comment:edit_own", "comment"),
    COMMENT_DELETE_OWN("comment:delete_own", "comment"),
    COMMENT_REPORT("comment:report", "comment"),
    COMMENT_DELETE_ANY("comment:delete_any", "comment"),

    // bookmark
    BOOKMARK_MANAGE("bookmark:manage", "bookmark"),

    // follow
    FOLLOW_MANAGE("follow:manage", "follow"),

    // rating
    RATING_MANAGE("rating:manage", "rating"),

    // user
    USER_VIEW("user:view", "user"),
    USER_BAN("user:ban", "user"),
    USER_UNBAN("user:unban", "user"),

    // creator
    CREATOR_VIEW_PENDING("creator:view_pending", "creator"),
    CREATOR_APPROVE("creator:approve", "creator"),
    CREATOR_REJECT("creator:reject", "creator"),
    CREATOR_SUSPEND("creator:suspend", "creator"),

    // content
    CONTENT_CREATE("content:create", "content"),
    CONTENT_EDIT_OWN("content:edit_own", "content"),
    CONTENT_DELETE_OWN("content:delete_own", "content"),
    CONTENT_PUBLISH_OWN("content:publish_own", "content"),
    CONTENT_REPORT("content:report", "content"),
    CONTENT_VIEW_REPORTS("content:view_reports", "content"),
    CONTENT_MODERATE("content:moderate", "content"),
    CONTENT_DELETE_ANY("content:delete_any", "content"),

    // system
    SYSTEM_SETTINGS("system:settings", "system"),
    SYSTEM_ROLES("system:roles", "system");

    private final String value;
    private final String category;

    GlobalPermissionName(String value, String category) {
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
