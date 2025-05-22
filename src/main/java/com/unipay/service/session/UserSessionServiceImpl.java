package com.unipay.service.session;

import com.unipay.models.User;
import com.unipay.models.UserSession;
import com.unipay.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;


/**
 * Service implementation for managing user sessions including creation, validation,
 * invalidation, and revocation logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserSessionServiceImpl implements UserSessionService {

    private static final Duration SESSION_REFRESH_THRESHOLD = Duration.ofMinutes(15);

    private final UserSessionRepository userSessionRepository;

    @Transactional
    public UserSession createSession(User user, String deviceId, String ipAddress, String userAgent, Instant expiresAt) {
        revokeExpiredSessions(user);

        UserSession session = UserSession.create(
                user,
                deviceId,
                ipAddress,
                userAgent
        );
        session.setExpiresAt(expiresAt);

        return userSessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public List<UserSession> getActiveSessions(User user) {
        return userSessionRepository.findByUserAndRevokedFalseAndExpiresAtAfter(
                user,
                Instant.now()
        );
    }
    /**
     * Validates a session by ID. If still valid, extends its expiration (optional).
     * @param sessionId the UUID or DB‐assigned ID of the session
     * @return the up‐to‐date UserSession, or null if invalid/expired/revoked
     */
    @Transactional
    public UserSession validateAndRefreshSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) return null;

        return userSessionRepository.findById(sessionId)
                .map(session -> {
                    if (!validateSession(session)) {
                        log.warn("Invalid session: {}", sessionId);
                        return null;
                    }

                    if (shouldRefreshSession(session)) {
                        session.setExpiresAt(Instant.now().plusMillis(800000));
                        userSessionRepository.save(session);
                        log.debug("Refreshed session expiration: {}", sessionId);
                    }

                    return session;
                })
                .orElse(null);
    }

    private boolean shouldRefreshSession(UserSession session) {
        Duration remaining = Duration.between(Instant.now(), session.getExpiresAt());
        return remaining.compareTo(SESSION_REFRESH_THRESHOLD) < 0;
    }

    @Transactional
    public void revokeSession(String sessionId) {
        userSessionRepository.findById(sessionId)
                .ifPresent(session -> {
                    session.setRevoked(true);
                    userSessionRepository.save(session);
                    log.info("Revoked session: {}", sessionId);
                });
    }

    @Transactional
    public void revokeAllSessions(User user) {
        userSessionRepository.revokeAllActiveSessions(
                user.getId(),
                Instant.now()
        );
        log.info("Revoked all sessions for user: {}", user.getEmail());
    }

    @Transactional
    public void revokeOtherSessions(User currentUser, String currentSessionId) {
        userSessionRepository.revokeOtherSessions(
                currentUser.getId(),
                currentSessionId,
                Instant.now()
        );
        log.info("Revoked other sessions for user: {}", currentUser.getEmail());
    }

    @Transactional
    public void revokeExpiredSessions(User user) {
        userSessionRepository.revokeExpiredSessions(
                user.getId(),
                Instant.now()
        );
    }
    /**
     * Returns true if there is already a non-revoked, unexpired session
     * for this user+deviceId.
     */
    @Transactional(readOnly = true)
    public boolean hasActiveSessionForDevice(User user, String deviceId) {
        return userSessionRepository.existsByUserAndDeviceIdAndRevokedFalseAndExpiresAtAfter(
                user, deviceId, Instant.now()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<UserSession> findActiveByUserAndDevice(User user, String deviceId, Instant now) {
        return userSessionRepository
                .findFirstByUserAndDeviceIdAndRevokedFalseAndExpiresAtAfter(user, deviceId, now);
    }

    public boolean validateSession(UserSession session) {
        return session != null &&
                !session.isRevoked() &&
                session.getExpiresAt().isAfter(Instant.now());
    }
}