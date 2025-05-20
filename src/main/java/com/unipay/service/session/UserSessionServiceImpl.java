package com.unipay.service.session;

import com.unipay.models.User;
import com.unipay.models.UserSession;
import com.unipay.repository.UserSessionRepository;
import com.unipay.utils.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;


/**
 * Service implementation for managing user sessions including creation, validation,
 * invalidation, and revocation logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserSessionServiceImpl implements UserSessionService {

    private final JwtService jwtService;
    private final UserSessionRepository sessionRepository;

    @Value("${session.expiration.days:7}")
    private int sessionExpirationDays;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserSession createSession(User user, String deviceId, String userAgent, String ipAddress) {
        Instant now = Instant.now();
        Optional<UserSession> existing = sessionRepository
                .findFirstByUserIdAndDeviceIdAndRevokedFalseAndExpiresAtAfter(
                        user.getId(), deviceId, now);

        if (existing.isPresent()) {
            UserSession session = existing.get();
            session.setExpiresAt(now.plus(sessionExpirationDays, ChronoUnit.DAYS));
            UserSession updated = sessionRepository.save(session);
            log.info("Extended session [{}] for user [{}]", updated.getId(), user.getId());
            return updated;
        }

        UserSession session = buildUserSession(user, deviceId, userAgent, ipAddress);
        UserSession saved = sessionRepository.save(session);
        log.info("Created new session [{}] for user [{}]", saved.getId(), user.getId());
        return saved;
    }

    private UserSession buildUserSession(User user, String deviceId, String userAgent, String ipAddress) {
        return UserSession.builder()
                .user(user)
                .deviceId(deviceId)
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .expiresAt(Instant.now().plus(sessionExpirationDays, ChronoUnit.DAYS))
                .revoked(false)
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void revokeSession(String sessionId) {
        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.setRevoked(true);
            jwtService.blacklistToken(sessionId);
            log.info("Revoked session [{}]", sessionId);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isSessionValid(String sessionId) {
        return sessionRepository.isValidSession(sessionId, Instant.now());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void invalidateSession(String sessionId) {
        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.setRevoked(true);
            sessionRepository.save(session);
            jwtService.blacklistToken(sessionId);
            log.info("Invalidated session [{}] and blacklisted token", sessionId);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void revokeAllSessions(User user) {
        int revokedCount = sessionRepository.bulkRevokeUserSessions(user.getId(), Instant.now());
        jwtService.bulkBlacklistTokens(user.getId());
        log.info("Revoked {} sessions for user [{}]", revokedCount, user.getId());
    }
}