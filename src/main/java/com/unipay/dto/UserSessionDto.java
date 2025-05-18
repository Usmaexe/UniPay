package com.unipay.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Represents an active session of a user in the system.
 * This class holds details about the session, including the device and IP address used to log in,
 * and the expiration time of the session.
 */
@Data
@Getter
@Setter
public class UserSessionDto {
    /**
     * Unique identifier for the session.
     */
    private String id;

    /**
     * Information about the device used to start the session (e.g., web browser, mobile app).
     */
    private String deviceId;

    /**
     * IP address of the user's device when the session was created.
     */
    private String ipAddress;
    private String userAgent;

    /**
     * The expiration timestamp of the session.
     * After this time, the session is considered expired and the user will need to log in again.
     */
    private Instant expiresAt;
}
