package com.unipay.service.user;

import com.unipay.command.UserRegisterCommand;
import com.unipay.criteria.UserCriteria;
import com.unipay.exception.BusinessException;
import com.unipay.models.User;
import jakarta.servlet.http.HttpServletRequest;
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
     * @param request The {@link HttpServletRequest} used to capture the user's IP address and user agent
     *                during the registration process for tracking purposes.
     * @return The newly created and fully initialized {@link User} entity, including the user's profile and settings.
     */
    User create(final UserRegisterCommand command, HttpServletRequest request);

    /**
     * Retrieves a user entity by its unique identifier.
     *
     * @param userId The ID of the user to retrieve.
     * @return The {@link User} entity matching the provided ID.
     */
    User getUserById(String userId);

    /**
     * Returns a paginated list of users filtered by the provided criteria.
     *
     * @param pageable The pagination and sorting information.
     * @param criteria The criteria used to filter the users.
     * @return A {@link Page} of {@link User} entities matching the specified criteria.
     */
    Page<User> getAllByCriteria(Pageable pageable, UserCriteria criteria);

    /**
     * Finds a user by email address, including their roles and associated permissions.
     *
     * @param email The email address of the user to find.
     * @return The {@link User} entity with roles and permissions loaded.
     */
    User findByEmailWithRolesAndPermissions(String email);

    /**
     * Retrieves a user by ID along with their roles.
     *
     * @param userId The ID of the user.
     * @return The {@link User} entity with roles initialized.
     */
    User getUserByIdWithRoles(String userId);
    /**
     * Initiates the "forgot password" workflow:
     *  1. Finds the user by email.
     *  2. Generates and saves a new confirmation token.
     *  3. Sends a password reset email containing that token.
     *
     * @param email the email address to send the reset link to
     * @throws BusinessException if the user is not found or email sending fails
     */
    void forgotPassword(String email);
    User findByEmail(String email);
    Optional<User> findByEmailWithOptional(String email);
    void changePassword(User user, String newPassword);
    void desactivateUser(String userId);
}
