package com.unipay.repository;

import com.unipay.criteria.UserCriteria;
import com.unipay.models.User;
import com.unipay.utils.UserSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entities.
 * Provides CRUD operations, custom queries, and dynamic specification support.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

    /**
     * Filters users based on the provided criteria and pagination.
     */
    default Page<User> getUsersByCriteria(Pageable pageable, UserCriteria criteria) {
        return findAll(new UserSpecification(criteria), pageable);
    }
    /**
     * Checks if a user exists by either email or username.
     */
    boolean existsByEmailOrUsername(String email, String userName);
    Optional<User> findByUsername(String username);
    @EntityGraph(attributePaths = {"userRoles.role.permissions"})
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
