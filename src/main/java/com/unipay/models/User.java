package com.unipay.models;

import com.unipay.command.UserRegisterCommand;
import com.unipay.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a user account within the system, encompassing authentication credentials,
 * profile information, roles, login history, settings, security questions, and audit logs.
 *
 * <p>Key Fields:
 * <ul>
 *   <li><strong>username</strong>: The unique username for the user.</li>
 *   <li><strong>email</strong>: The unique email address associated with the user.</li>
 *   <li><strong>passwordHash</strong>: The hashed password for authentication.</li>
 *   <li><strong>status</strong>: The current status of the user account (e.g., ACTIVE, INACTIVE, SUSPENDED).</li>
 *   <li><strong>profile</strong>: The user's profile containing personal information.</li>
 *   <li><strong>roles</strong>: The set of roles assigned to the user.</li>
 *   <li><strong>loginHistories</strong>: Records of the user's login attempts.</li>
 *   <li><strong>settings</strong>: User-specific settings and preferences.</li>
 *   <li><strong>securityQuestions</strong>: Security questions for account recovery.</li>
 *   <li><strong>auditLogs</strong>: Logs of actions performed by the user within the system.</li>
 * </ul>
 *
 * <p>This class serves as the central entity for user management, containing all relevant details
 * about the user’s account, authentication, and associated information. It supports user registration,
 * login history tracking, and role management, among other features.
 */
@Builder
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    /**
     * The unique username for the user.
     * This field is required to be unique to ensure each user has a distinct identifier.
     */
    @Column(nullable = false, unique = true)
    private String username;

    /**
     * The unique email address associated with the user.
     * This field is also unique to ensure there are no duplicate email addresses in the system.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * The hashed password for authentication.
     * Storing the password in a hashed format ensures it is securely stored and protected.
     */
    @Column(nullable = false)
    private String passwordHash;

    /**
     * The current status of the user account.
     * The status is represented by an enumerated value from {@link UserStatus}.
     */
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    /**
     * The user's profile containing personal information such as name, address, and other details.
     * This is a one-to-one relationship with the {@link UserProfile} entity.
     */
    @OneToOne(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private UserProfile profile;

    /**
     * The set of roles assigned to the user.
     * This relationship supports role-based access control (RBAC), defining what actions the user can perform.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<UserRole> userRoles = new HashSet<>();

    /**
     * Records of the user's login attempts.
     * This is useful for auditing and tracking user login patterns for security purposes.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<LoginHistory> loginHistories = new HashSet<>();

    /**
     * User-specific settings and preferences, such as notification preferences and language settings.
     * This is a one-to-one relationship with the {@link UserSettings} entity.
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserSettings settings;

    /**
     * Security questions associated with the user's account for identity verification.
     * This is important for account recovery and additional authentication.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<SecurityQuestion> securityQuestions = new HashSet<>();

    /**
     * Logs of actions performed by the user within the system, including administrative actions.
     * This allows for tracking user activities for security and auditing purposes.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<AuditLog> auditLogs = new HashSet<>();

    @OneToOne(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private MFASettings mfaSettings;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private Set<UserSession> sessions = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Consent> consents = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Business> businesses = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Transaction> transactions = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ApiKey> apiKeys = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Webhook> webhooks = new HashSet<>();

    /**
     * Creates a new {@link User} instance from the provided {@link UserRegisterCommand}.
     *
     * @param command The command containing the user's registration data.
     * @return A new {@link User} object initialized with the command's values.
     */
    public static User create(final UserRegisterCommand command){
        final User user = new User();

        user.username = command.getUsername();
        user.email = command.getEmail();
        user.passwordHash = command.getPassword();
        return user;
    }
    /**
     * Adds a login history record to the user's login history.
     * This method links the provided {@link LoginHistory} to the user.
     * It also ensures that the login history is correctly associated with the user.
     *
     * @param loginHistory The {@link LoginHistory} object to be linked with the user.
     */
    public void addLoginHistory(LoginHistory loginHistory) {
        // Initialize the set if it's null
        if (this.loginHistories == null) {
            this.loginHistories = new HashSet<>();
        }
        this.loginHistories.add(loginHistory);
        loginHistory.setUser(this);
    }
    /**
     * Adds an AuditLog entry to the user's audit log history.
     * This method ensures that the audit log is properly associated with the user and the collection is not null.
     *
     * @param auditLog The {@link AuditLog} object to be added to the user's audit logs.
     */
    public void addAuditLog(AuditLog auditLog) {
        if (this.auditLogs == null) {
            this.auditLogs = new HashSet<>();
        }
        this.auditLogs.add(auditLog);
        auditLog.setUser(this);
    }
}
