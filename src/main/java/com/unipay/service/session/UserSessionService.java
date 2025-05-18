package com.unipay.service.session;

import com.unipay.models.User;
import com.unipay.models.UserSession;

/**
 * Service interface for managing user sessions.
 * Provides functionality for session creation, validation, and invalidation.
 */
public interface UserSessionService {

    /**
     * Checks whether the provided session ID corresponds to a valid and active session.
     *
     * @param sessionId The ID of the session to validate.
     * @return {@code true} if the session is valid and not expired or revoked, otherwise {@code false}.
     */
    boolean isSessionValid(String sessionId);

    /**
     * Invalidates the session associated with the given session ID by marking it as revoked.
     *
     * @param sessionId The ID of the session to invalidate.
     */
    void invalidateSession(String sessionId);

    /**
     * Creates a new session for the specified user with the given user agent and IP address.
     *
     * @param user      The user for whom the session is created.
     * @param userAgent The user agent string from the client.
     * @param ipAddress The IP address of the client.
     * @return A new {@link UserSession} instance representing the created session.
     */
    UserSession createSession(User user, String deviceId,  String userAgent, String ipAddress);
    void revokeAllSessions(User user);
    void revokeSession(String sessionId);
}
