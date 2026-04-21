package com.syuro.wibusystem.shared.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handle(AppException ex) {
        String message = messageSource.getMessage(
                ex.getCode().name(),
                ex.getArgs(),
                LocaleContextHolder.getLocale()
        );
        return ResponseEntity
                .status(ex.getCode().status())
                .body(ErrorResponse.of(ex.getCode().name(), message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handle(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        e -> e.getDefaultMessage() != null ? e.getDefaultMessage() : "Invalid value",
                        (first, second) -> first
                ));

        String message = messageSource.getMessage(
                ErrorCode.VALIDATION_FAILED.name(),
                null,
                LocaleContextHolder.getLocale()
        );
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.ofValidation(message, fieldErrors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handle(Exception ex) {
        log.error("Unhandled exception", ex);
        String message = messageSource.getMessage(
                ErrorCode.INTERNAL_SERVER_ERROR.name(),
                null,
                LocaleContextHolder.getLocale()
        );
        return ResponseEntity
                .internalServerError()
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR.name(), message));
    }
}
