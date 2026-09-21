package com.financetracker.exception;   

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field   = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(field, message);
        });
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation failed", errors));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailExists(EmailAlreadyExistsException ex) {
        return conflict(ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return unauthorized(ex.getMessage());
    }

    @ExceptionHandler(AccountNotVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleNotVerified(AccountNotVerifiedException ex) {
        return forbidden(ex.getMessage());
    }

    @ExceptionHandler({InvalidTokenException.class, TokenExpiredException.class})
    public ResponseEntity<ErrorResponse> handleToken(RuntimeException ex) {
        return badRequest(ex.getMessage());
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ErrorResponse> handlePasswordMismatch(PasswordMismatchException ex) {
        return badRequest(ex.getMessage());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UsernameNotFoundException ex) {
        return notFound(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse(500, "An unexpected error occurred", null));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ResponseEntity<ErrorResponse> badRequest(String msg) {
        return ResponseEntity.badRequest().body(new ErrorResponse(400, msg, null));
    }

    private ResponseEntity<ErrorResponse> unauthorized(String msg) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(401, msg, null));
    }

    private ResponseEntity<ErrorResponse> forbidden(String msg) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(403, msg, null));
    }

    private ResponseEntity<ErrorResponse> notFound(String msg) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, msg, null));
    }

    private ResponseEntity<ErrorResponse> conflict(String msg) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(409, msg, null));
    }

    // ── Error response record ─────────────────────────────────────────────────

    public record ErrorResponse(
            int status,
            String message,
            Map<String, String> errors,
            LocalDateTime timestamp
    ) {
        public ErrorResponse(int status, String message, Map<String, String> errors) {
            this(status, message, errors, LocalDateTime.now());
        }
    }
}
