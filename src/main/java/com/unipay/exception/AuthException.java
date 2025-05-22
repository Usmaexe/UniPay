package com.unipay.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * A runtime exception to signal authentication/authorization failures
 * with an associated HTTP status code.
 */
public class AuthException extends ResponseStatusException {

    public AuthException(String reason, HttpStatus status) {
        super(status, reason);
    }

    public AuthException(HttpStatus status) {
        super(status);
    }

    public AuthException(HttpStatus status, String reason, Throwable cause) {
        super(status, reason, cause);
    }
}

