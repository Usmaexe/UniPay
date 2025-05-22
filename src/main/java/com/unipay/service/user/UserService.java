package com.unipay.service.user;

import com.unipay.command.UserRegisterCommand;
import com.unipay.criteria.UserCriteria;
import com.unipay.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Service interface for managing core user operations within the system.
 *
 * <p>This interface defines methods related to the lifecycle of {@link User} entities,
 * including creation, retrieval, and filtering.</p>
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Creating new users based on validated registration commands.</li>
 *   <li>Retrieving users by ID, email, or filtered search criteria.</li>
 *   <li>Supporting user queries with role and permission loading.</li>
 * </ul>
 * </p>
 *
 * @see com.unipay.command.UserRegisterCommand
 * @see com.unipay.models.User
 */
public interface UserService {
    /**
     * Registers a new user in the system based on the provided registration command and request details.
     * <p>
     * This method creates a new user, along with their associated profile and settings, and logs the user's
     * login attempt. The process ensures that all related data is persisted within a transactional context.
     * </p>
     *
     * @param command The {@link UserRegisterCommand} containing validated registration information, such as
     *                the user's credentials, profile, and settings.
     *                during the registration process for tracking purposes.
     * @return The newly created and fully initialized {@link User} entity, including the user's profile and settings.
     */
    User create(final UserRegisterCommand command);
    /**
     * Returns a paginated list of users filtered by the provided criteria.
     *
     * @param pageable The pagination and sorting information.
     * @param criteria The criteria used to filter the users.
     * @return A {@link Page} of {@link User} entities matching the specified criteria.
     */
    Page<User> getAllByCriteria(Pageable pageable, UserCriteria criteria);
    User findByEmailWithNoOptional(String email);
    Optional<User> findByEmail(String email);
    User findByUsername(String username);
}
