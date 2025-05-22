package com.unipay.repository;

import com.unipay.models.User;
import com.unipay.models.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;


@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, String> {
    @Modifying
    @Query("UPDATE UserSession s SET s.revoked = true " +
            "WHERE s.user.id = :userId AND s.expiresAt > :now")
    void revokeAllActiveSessions(@Param("userId") String userId, @Param("now") Instant now);

    @Modifying
    @Query("UPDATE UserSession s SET s.revoked = true " +
            "WHERE s.user.id = :userId AND s.id != :currentSessionId AND s.expiresAt > :now")
    void revokeOtherSessions(@Param("userId") String userId,
                             @Param("currentSessionId") String currentSessionId,
                             @Param("now") Instant now);

    @Modifying
    @Query("UPDATE UserSession s SET s.revoked = true " +
            "WHERE s.user.id = :userId AND s.expiresAt <= :now")
    void revokeExpiredSessions(@Param("userId") String userId, @Param("now") Instant now);

    List<UserSession> findByUserAndRevokedFalseAndExpiresAtAfter(User user, Instant now);
    boolean existsByUserAndDeviceIdAndRevokedFalseAndExpiresAtAfter(User user, String deviceId, Instant now);
    Optional<UserSession> findFirstByUserAndDeviceIdAndRevokedFalseAndExpiresAtAfter(
            User user, String deviceId, Instant now
    );;
}
