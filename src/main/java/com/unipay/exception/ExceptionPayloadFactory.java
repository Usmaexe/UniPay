package com.unipay.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Enum to define different types of exception payloads that are used to generate
 * specific exception responses based on error types. Each enum constant represents
 * an exception case, providing a code, HTTP status, and a message key.
 *
 * It provides a method to retrieve an `ExceptionPayload` for each error type.
 */
@Getter
@AllArgsConstructor
public enum ExceptionPayloadFactory {

    TECHNICAL_ERROR(0, HttpStatus.INTERNAL_SERVER_ERROR, "technical.error"),
    INVALID_PAYLOAD(1, HttpStatus.BAD_REQUEST, "invalid.request.payload"),
    MISSING_REQUEST_BODY_ERROR_CODE(2, HttpStatus.BAD_REQUEST, "request.missing.body"),
    USER_NOT_FOUND(3, HttpStatus.NOT_FOUND, "user.not.found"),
    USER_ALREADY_EXIST(4, HttpStatus.CONFLICT, "user.already.exist"),
    ROLE_NOT_FOUND(5, HttpStatus.NOT_FOUND, "role.not.found"),
    CONFIRMATION_TOKEN_NOT_FOUND(6, HttpStatus.NOT_FOUND, "confirmation.token.not.found"),
    PERMISSION_NOT_FOUND(7, HttpStatus.NOT_FOUND, "permission.not.found"),
    AUDIT_LOG_NOT_FOUND(8, HttpStatus.NOT_FOUND, "audit.log.not.found"),
    USER_NOT_ACTIVE(9, HttpStatus.FORBIDDEN, "user.not.active"),
    AUTHENTICATION_FAILED(10, HttpStatus.UNAUTHORIZED, "authentication.failed"),
    FAILED_TO_SEND_EMAIL(11, HttpStatus.FAILED_DEPENDENCY, "failed.to.send.email"),
    MFA_NOT_SET_UP(12, HttpStatus.BAD_REQUEST, "mfa.not.set.up"),
    INVALID_MFA_CODE(13, HttpStatus.UNAUTHORIZED, "invalid.mfa.code"),
    MFA_NOT_ENABLED(14, HttpStatus.FORBIDDEN, "mfa.not.enabled"),
    INVALID_MFA_CHALLENGE(15, HttpStatus.GONE, "invalid.or.expired.mfa.challenge"),
    INVALID_SESSION(16, HttpStatus.UNAUTHORIZED, "invalid.or.expired.session"),
    INVALID_TOKEN(17,HttpStatus.UNAUTHORIZED, "invalid.or.malformed.token"),
    BUSINESS_NOT_FOUND(18,HttpStatus.NOT_FOUND, "business.not.found"),
    BUSINESS_FOR_USER_NOT_FOUND(19,HttpStatus.NOT_FOUND, "business.for.user.not.found"),
    USER_PROFILE_NOT_FOUND(20, HttpStatus.NOT_FOUND, "user.profile.not.found"),
    USER_NOT_AUTHENTICATED(21, HttpStatus.BAD_REQUEST, "user.not.authenticated");

    private final Integer code;
    private final HttpStatus status;
    private final String message;

    /**
     * Creates an `ExceptionPayload` instance from the enum constants.
     * @return An `ExceptionPayload` instance containing code, status, and message.
     */
    public ExceptionPayload get() {
        return new ExceptionPayload(code, status, message);
    }
}
