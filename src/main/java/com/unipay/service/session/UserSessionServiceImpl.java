package com.unipay.service.session;

import com.unipay.models.User;
import com.unipay.models.UserSession;
import com.unipay.repository.UserSessionRepository;
import com.unipay.utils.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

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

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserSession createSession(User user,String deviceId,  String userAgent, String ipAddress) {
        UserSession session = buildUserSession(user, deviceId, userAgent, ipAddress);
        UserSession savedSession = sessionRepository.save(session);
        log.info("Created session [{}] for user [{}]", savedSession.getId(), user.getId());
        return savedSession;
    }

    private UserSession buildUserSession(User user, String deviceId, String userAgent, String ipAddress) {
        return UserSession.builder()
                .user(user)
                .deviceId(deviceId)
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .revoked(false)
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void revokeSession(String sessionId) {
        sessionRepository.deleteById(sessionId);
        log.info("Revoked session [{}]", sessionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isSessionValid(String sessionId) {
        return sessionRepository.findById(sessionId)
                .map(session -> !session.isRevoked() && session.getExpiresAt().isAfter(Instant.now()))
                .orElse(false);
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
        List<UserSession> sessions = sessionRepository.findByUser(user);

        for (UserSession session : sessions) {
            session.setRevoked(true);
            sessionRepository.save(session);
            jwtService.blacklistToken(session.getId());
            log.debug("Revoked session [{}] for user [{}]", session.getId(), user.getId());
        }

        log.info("Revoked all sessions for user [{}]", user.getId());
    }
}
