package com.unipay.repository;

import com.unipay.criteria.UserCriteria;
import com.unipay.models.User;
import com.unipay.utils.UserSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;

/**
 * Repository interface for User entities.
 * Provides CRUD operations, custom queries, and dynamic specification support.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

    /**
     * Finds a user by username or email, fetching associated roles and permissions eagerly.
     */
    @EntityGraph(attributePaths = {
            "userRoles",
            "userRoles.role",
            "userRoles.role.permissions"
    })
    Optional<User> findByUsernameOrEmail(String username, String email);

    /**
     * Checks if a user exists by either email or username.
     */
    boolean existsByEmailOrUsername(String email, String userName);

    /**
     * Filters users based on the provided criteria and pagination.
     */
    default Page<User> getUsersByCriteria(Pageable pageable, UserCriteria criteria) {
        return findAll(new UserSpecification(criteria), pageable);
    }

    /**
     * Finds a user by email and fetches their roles, permissions, sessions, and MFA settings eagerly.
     */
    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.userRoles ur " +
            "LEFT JOIN FETCH ur.role r " +
            "LEFT JOIN FETCH r.permissions " +
            "LEFT JOIN FETCH u.sessions s " +
            "LEFT JOIN FETCH u.mfaSettings m " +
            "WHERE u.email = :email")
    Optional<User> findByEmailWithRolesAndPermissions(@Param("email") String email);

    /**
     * Finds a user by ID and fetches their MFA settings.
     */
    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.mfaSettings m " +
            "WHERE u.id = :userId")
    Optional<User> findByIdWithMfaSettings(@Param("userId") String userId);

    /**
     * Finds a user by ID and fetches their roles and permissions.
     */
    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.userRoles ur " +
            "LEFT JOIN FETCH ur.role r " +
            "LEFT JOIN FETCH r.permissions " +
            "WHERE u.id = :userId")
    Optional<User> findByIdWithRoles(@Param("userId") String userId);

    /**
     * Revokes all sessions for a given user by marking them revoked.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE UserSession s " +
            "SET s.revoked = true " +
            "WHERE s.user.id = :userId " +
            "AND s.revoked = false")
    int bulkRevokeUserSessions(@Param("userId") String userId);
    Optional<User> findByEmail(String email);
}
