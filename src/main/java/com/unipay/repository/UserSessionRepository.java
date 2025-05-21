package com.unipay.repository;

import com.unipay.models.User;
import com.unipay.models.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;


@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, String> {
    void deleteByUser(User user);
    List<UserSession> findByUser(User user);
    Optional<UserSession> findFirstByUserIdAndDeviceIdAndRevokedFalseAndExpiresAtAfter(String userId, String deviceId, Instant now);

    /**
     * Bulk-revoke all unexpired, non-revoked sessions for a user.
     * @param userId the user's ID
     * @param now current timestamp to compare expiry
     * @return number of sessions revoked
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE UserSession s " +
            "SET s.revoked = true " +
            "WHERE s.user.id = :userId " +
            "AND s.revoked = false " +
            "AND s.expiresAt > :now")
    int bulkRevokeUserSessions(@Param("userId") String userId,
                               @Param("now") Instant now);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM UserSession s WHERE s.id = :sessionId " +
            "AND s.revoked = false " +
            "AND s.expiresAt > :now")
    boolean isValidSession(@Param("sessionId") String sessionId, @Param("now") Instant now);
}
