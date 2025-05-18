package com.unipay.repository;

import com.unipay.criteria.UserCriteria;
import com.unipay.models.User;
import com.unipay.utils.UserSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entities.
 * Provides CRUD operations, custom queries, and dynamic specification support.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

    /**
     * Finds a user by username or email, fetching associated roles and permissions eagerly.
     *
     * @param username the username
     * @param email    the email
     * @return Optional containing the user if found
     */
    @EntityGraph(attributePaths = {"roles", "roles.role", "roles.role.permissions"})
    Optional<User> findByUsernameOrEmail(String username, String email);

    /**
     * Finds a user by ID.
     *
     * @param userId the user ID
     * @return Optional containing the user if found
     */
    Optional<User> findById(String userId);

    /**
     * Finds a user by email.
     *
     * @param email the email
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists by either email or username.
     *
     * @param email    the email
     * @param userName the username
     * @return true if the user exists, false otherwise
     */
    boolean existsByEmailOrUsername(String email, String userName);

    /**
     * Filters users based on the provided criteria and pagination.
     *
     * @param pageable the pagination information
     * @param criteria the filtering criteria
     * @return A page of users that match the criteria
     */
    default Page<User> getUsersByCriteria(Pageable pageable, UserCriteria criteria) {
        return findAll(new UserSpecification(criteria), pageable);
    }

    /**
     * Finds a user by email and fetches their roles and permissions eagerly.
     *
     * @param email the email
     * @return Optional containing the user if found
     */
    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.sessions " +
            "LEFT JOIN FETCH u.userRoles ur " +
            "LEFT JOIN FETCH ur.role r " +
            "LEFT JOIN FETCH r.permissions " +
            "LEFT JOIN FETCH u.mfaSettings " +
            "WHERE u.email = :email")
    Optional<User> findByEmailWithRolesAndPermissions(@Param("email") String email);

    /**
     * Finds a user by ID and fetches their MFA settings.
     *
     * @param userId the user ID
     * @return Optional containing the user if found
     */
    @Query("""
           SELECT u FROM User u 
           LEFT JOIN FETCH u.mfaSettings 
           WHERE u.id = :userId
           """)
    Optional<User> findByIdWithMfaSettings(@Param("userId") String userId);

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.userRoles ur " +
            "LEFT JOIN FETCH ur.role r " +
            "LEFT JOIN FETCH r.permissions " +
            "WHERE u.id = :userId")
    Optional<User> findByIdWithRoles(@Param("userId") String userId);
}
