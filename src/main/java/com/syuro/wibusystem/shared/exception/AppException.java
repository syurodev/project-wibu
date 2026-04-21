package com.syuro.wibusystem.shared.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

    private final ErrorCode code;
    private final Object[] args;

    public AppException(ErrorCode code) {
        super(code.name());
        this.code = code;
        this.args = new Object[0];
    }

    public AppException(ErrorCode code, Object... args) {
        super(code.name());
        this.code = code;
        this.args = args;
    }
}
