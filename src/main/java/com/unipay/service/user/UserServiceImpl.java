package com.unipay.service.user;

import com.unipay.command.UserRegisterCommand;
import com.unipay.criteria.UserCriteria;
import com.unipay.enums.AuditLogAction;
import com.unipay.enums.RoleName;
import com.unipay.enums.UserStatus;
import com.unipay.exception.BusinessException;
import com.unipay.exception.ExceptionPayloadFactory;
import com.unipay.helper.UserRegistrationHelper;
import com.unipay.models.Business;
import com.unipay.models.ConfirmationToken;
import com.unipay.models.MFASettings;
import com.unipay.models.User;
import com.unipay.repository.ConfirmationTokenRepository;
import com.unipay.repository.UserRepository;
import com.unipay.service.audit_log.AuditLogService;
import com.unipay.service.mail.EmailService;
import com.unipay.service.role.RoleService;
import com.unipay.service.session.UserSessionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service implementation for handling user registration, retrieval, and related logic.
 *
 * <p>This implementation manages user creation along with their profile, settings,
 * MFA configuration, and initial role assignment. It ensures all processes occur
 * within transactional boundaries and logs important lifecycle events.</p>
 *
 * <p>Main responsibilities:
 * <ul>
 *   <li>Registering new users and initializing associated entities.</li>
 *   <li>Assigning default roles and auditing the creation action.</li>
 *   <li>Sending confirmation emails and managing MFA settings.</li>
 *   <li>Querying user data by criteria or identity with role/permission enrichment.</li>
 * </ul>
 * </p>
 *
 * @see UserService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final AuditLogService auditLogService;
    private final UserSessionService userSessionService;
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final UserRegistrationHelper registrationHelper;
    private final EmailService emailService;

    /**
     * Registers a new user by persisting their base account, MFA, profile, and settings.
     * Also assigns the USER role and initiates email verification.
     *
     * @param command The registration command with username, password, email, and profile data.
     * @param request HTTP request used for logging the origin IP and user-agent.
     * @return The newly created {@link User} with associated entities.
     */
    @Override
    @Transactional
    public User create(UserRegisterCommand command, HttpServletRequest request) {
        log.debug("Registering user: {}", command.getUsername());
        validateUserDoesNotExist(command);

        User user = buildUser(command);
        assignUserRole(user);
        registrationHelper.associateUserProfileAndSettings(user, command);
        registrationHelper.auditLogCreate(user, AuditLogAction.USER_CREATED.getAction(), "User created");

        sendConfirmation(user);
        user.setStatus(UserStatus.PENDING);
        return user;
    }

    private void validateUserDoesNotExist(UserRegisterCommand command) {
        if (userRepository.existsByEmailOrUsername(command.getEmail(), command.getUsername())) {
            throw new BusinessException(ExceptionPayloadFactory.USER_ALREADY_EXIST.get());
        }
    }

    private User buildUser(UserRegisterCommand command) {
        User user = User.create(command);
        user.setPasswordHash(passwordEncoder.encode(command.getPassword()));
        initializeMfaSettings(user);
        return userRepository.saveAndFlush(user);
    }

    private void assignUserRole(User user) {
        roleService.assignRoleToUser(user, RoleName.USER);
    }

    private void sendConfirmation(User user) {
        emailService.sendConfirmationEmail(user);
    }

    /**
     * Initializes MFA settings for a newly registered user. By default, MFA is disabled.
     *
     * @param user The user for whom to configure MFA settings.
     */
    private void initializeMfaSettings(User user) {
        MFASettings mfaSettings = new MFASettings();
        mfaSettings.setEnabled(false);
        mfaSettings.setUser(user);
        user.setMfaSettings(mfaSettings);
    }

    /**
     * Validates if a user already exists with the same email or username.
     * Throws a {@link BusinessException} if a conflict is found.
     *
     * @param command The registration input.
     */
    private void checkIfUserExists(UserRegisterCommand command) {
        if (userRepository.existsByEmailOrUsername(command.getEmail(), command.getUsername())) {
            log.warn("User with email {} or username {} already exists", command.getEmail(), command.getUsername());
            throw new BusinessException(ExceptionPayloadFactory.USER_ALREADY_EXIST.get());
        }
    }

    /**
     * Retrieves a user entity by its unique identifier.
     *
     * @param userId The user ID.
     * @return The {@link User} found or throws if not found.
     */
    @Override
    @Transactional(readOnly = true)
    public User getUserById(String userId) {
        log.debug("Fetching user with ID {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ExceptionPayloadFactory.USER_NOT_FOUND.get()));
    }

    /**
     * Retrieves a user and eagerly loads their roles and permissions.
     *
     * @param email The user's email address.
     * @return The {@link User} with roles and permissions.
     */
    @Override
    public User findByEmailWithRolesAndPermissions(String email) {
        log.info("Fetching user by email: {}", email);
        return userRepository.findByEmailWithRolesAndPermissions(email)
                .orElseThrow(() -> new BusinessException(ExceptionPayloadFactory.USER_NOT_FOUND.get()));
    }

    /**
     * Retrieves a paginated list of users based on filtering criteria.
     *
     * @param pageable Pagination and sorting info.
     * @param criteria Filters to apply.
     * @return A paginated list of {@link User} entities.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<User> getAllByCriteria(Pageable pageable, UserCriteria criteria) {
        try {
            Page<User> users = userRepository.getUsersByCriteria(pageable, criteria);
            log.debug("Users fetched by criteria successfully.");
            return users;
        } catch (Exception e) {
            log.error("Error fetching users by criteria", e);
            throw new BusinessException(ExceptionPayloadFactory.TECHNICAL_ERROR.get(), e);
        }
    }

    /**
     * Retrieves a user by ID and includes their roles.
     *
     * @param userId The ID of the user.
     * @return The {@link User} with roles initialized.
     */
    @Override
    @Transactional(readOnly = true)
    public User getUserByIdWithRoles(String userId) {
        return userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new BusinessException(ExceptionPayloadFactory.USER_NOT_FOUND.get()));
    }

    /**
     * Initiates the "forgot password" workflow:
     *  1. Finds the user by email.
     *  2. Generates and saves a new confirmation token.
     *  3. Sends a password reset email containing that token.
     *
     * @param email the email address to send the reset link to
     * @throws BusinessException if the user is not found or email sending fails
     */
    @Transactional
    public void forgotPassword(String email) {
        log.info("Initiating forgot-password workflow for [{}]", email);
        try {
            User user = findByEmail(email);

            ConfirmationToken token = ConfirmationToken.create(user);
            confirmationTokenRepository.save(token);
            log.debug("Generated ConfirmationToken [{}] for user [{}]", token.getConfirmationToken(), email);

            emailService.sendPasswordResetEmail(user, token.getConfirmationToken());
            log.info("Password reset email dispatched to [{}]", email);

        } catch (BusinessException be) {
            throw be;
        } catch (Exception ex) {
            log.error("Error in forgotPassword for [{}]", email, ex);
            throw new BusinessException(ExceptionPayloadFactory.TECHNICAL_ERROR.get(), ex);
        }
    }
    @Override
    public User findByEmail(String email){
        return userRepository.findByEmail(email).orElseThrow(
                () -> new BusinessException(ExceptionPayloadFactory.USER_NOT_FOUND.get())
        );
    }

    @Override
    public Optional<User> findByEmailWithOptional(String email) {
        return userRepository.findByEmail(email);
    }
    @Override
    @Transactional
    public void changePassword(User user, String newPassword) {
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userSessionService.revokeAllSessions(user);
        auditLogService.createAuditLog(
                user,
                AuditLogAction.PASSWORD_CHANGED.getAction(),
                "Password changed - sessions revoked"
        );
    }

    @Override
    @Transactional
    public void deactivateUser(String userId) {
        User user = getUserById(userId);
        user.setStatus(UserStatus.INACTIVE);
        userSessionService.revokeAllSessions(user);
        auditLogService.createAuditLog(
                user,
                AuditLogAction.ACCOUNT_LOCKED.getAction(),
                "Account deactivated - sessions revoked"
        );
    }
}
