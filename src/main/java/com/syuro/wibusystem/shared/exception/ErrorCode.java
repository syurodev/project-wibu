package com.syuro.wibusystem.shared.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // auth
    EMAIL_ALREADY_IN_USE(HttpStatus.CONFLICT),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED),
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED),
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND),
    OTP_INVALID(HttpStatus.BAD_REQUEST),
    OTP_EXPIRED(HttpStatus.GONE),
    MAGIC_LINK_INVALID(HttpStatus.UNAUTHORIZED),
    MAGIC_LINK_EXPIRED(HttpStatus.GONE),
    PASSKEY_CHALLENGE_EXPIRED(HttpStatus.GONE),
    PASSKEY_VERIFICATION_FAILED(HttpStatus.UNAUTHORIZED),
    PASSKEY_CREDENTIAL_NOT_FOUND(HttpStatus.NOT_FOUND),
    PASSKEY_CREDENTIAL_ALREADY_EXISTS(HttpStatus.CONFLICT),

    // user
    USER_NOT_FOUND(HttpStatus.NOT_FOUND),
    USER_BANNED(HttpStatus.FORBIDDEN),

    // rbac
    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND),
    ORG_ROLE_NOT_FOUND(HttpStatus.NOT_FOUND),

    // master data — genre
    GENRE_NOT_FOUND(HttpStatus.NOT_FOUND),
    GENRE_SLUG_CONFLICT(HttpStatus.CONFLICT),

    // master data — organization
    ORGANIZATION_NOT_FOUND(HttpStatus.NOT_FOUND),
    ORGANIZATION_SLUG_CONFLICT(HttpStatus.CONFLICT),

    // master data — people
    PEOPLE_NOT_FOUND(HttpStatus.NOT_FOUND),
    PEOPLE_SLUG_CONFLICT(HttpStatus.CONFLICT),

    // creator profile
    CREATOR_PROFILE_ALREADY_EXISTS(HttpStatus.CONFLICT),
    CREATOR_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND),
    CREATOR_PROFILE_SLUG_CONFLICT(HttpStatus.CONFLICT),
    CREATOR_PROFILE_NOT_DELETED(HttpStatus.CONFLICT),

    // work
    WORK_NOT_FOUND(HttpStatus.NOT_FOUND),
    WORK_ALREADY_ONESHOT(HttpStatus.CONFLICT),
    WORK_ALREADY_SERIES(HttpStatus.CONFLICT),
    WORK_TOO_MANY_CHAPTERS_FOR_ONESHOT(HttpStatus.CONFLICT),

    // common
    FORBIDDEN(HttpStatus.FORBIDDEN),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
