package com.unipay.service.session;

import com.unipay.models.User;
import com.unipay.models.UserSession;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing user sessions.
 * Provides functionality for session creation, validation, and invalidation.
 */
public interface UserSessionService {
    UserSession createSession(User user, String deviceId, String ipAddress, String userAgent, Instant expiresAt);
    List<UserSession> getActiveSessions(User user);
    UserSession validateAndRefreshSession(String sessionId);
    void revokeSession(String sessionId);
    void revokeAllSessions(User user);
    void revokeOtherSessions(User currentUser, String currentSessionId);
    void revokeExpiredSessions(User user);
    boolean hasActiveSessionForDevice(User user, String deviceId);
    Optional<UserSession> findActiveByUserAndDevice(User user, String deviceId, Instant now);
}
