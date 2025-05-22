package com.unipay.models;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a record of a user's login attempt within the system.
 * Captures essential details such as the timestamp of the login,
 * the IP address from which the attempt was made, the user agent
 * string of the client's browser or application, and whether the
 * login attempt was successful.
 *
 * <p>This entity aids in monitoring user access patterns, detecting
 * unauthorized access attempts, and auditing user authentication
 * activities.
 *
 * <p>Key Features:
 * <ul>
 *   <li><strong>user</strong>: The user who attempted to log in. It is a relationship with the {@link User} entity.</li>
 *   <li><strong>loginTimestamp</strong>: The timestamp marking when the login attempt occurred.</li>
 *   <li><strong>ipAddress</strong>: The IP address from which the login attempt was made.</li>
 *   <li><strong>userAgent</strong>: The user agent string of the client's browser or application.</li>
 *   <li><strong>successful</strong>: A boolean flag indicating whether the login attempt was successful.</li>
 * </ul>
 *
 * <p>This class helps track login attempts for each user, which is crucial for:
 * <ul>
 *   <li>Auditing user login activity.</li>
 *   <li>Monitoring security and detecting suspicious login attempts.</li>
 *   <li>Providing historical login data for users.</li>
 * </ul>
 *
 * @see com.unipay.models.User
 */
@Getter
@Setter
@Entity
@Table(name = "login_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginHistory extends BaseEntity {

    /**
     * The user who attempted to log in. This is a mandatory relationship with the {@link User} entity.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * The timestamp marking when the login attempt occurred.
     * This value is automatically set when a login record is created.
     */
    private LocalDateTime loginTimestamp;

    /**
     * The IP address from which the login attempt was made.
     * This helps in tracking the location or origin of the login attempt.
     */
    private String ipAddress;

    /**
     * The user agent string of the client's browser or application.
     * This provides information about the client device and software.
     */
    private String userAgent;

    /**
     * A boolean flag indicating whether the login attempt was successful.
     * A value of true means the login attempt succeeded, and false means it failed.
     */
    private boolean successful;

    public static LoginHistory create(User user, boolean successful){
        final LoginHistory loginHistory = new LoginHistory();

        loginHistory.user = user;
        loginHistory.loginTimestamp = LocalDateTime.now();
        loginHistory.successful = successful;

        return loginHistory;
    }
    /**
     * Factory for a successful login history entry.
     *
     * @param user        the user who logged in
     * @param timestamp   when the login occurred
     * @param ipAddress   source IP of the login
     * @param userAgent   client user-agent string
     * @return a new LoginHistory marked successful
     */
    public static LoginHistory createSuccess(
            User user,
            LocalDateTime timestamp,
            String ipAddress,
            String userAgent
    ) {
        LoginHistory loginHistory = new LoginHistory();
        loginHistory.user = user;
        loginHistory.loginTimestamp = timestamp;
        loginHistory.ipAddress = ipAddress;
        loginHistory.userAgent = userAgent;
        loginHistory.successful = true;
        return loginHistory;
    }
    public static LoginHistory createFailure(
            User user,
            LocalDateTime timestamp,
            String ipAddress,
            String userAgent,
            String failureReason
    ) {
        LoginHistory loginHistory = new LoginHistory();
        loginHistory.user = user;
        loginHistory.loginTimestamp = timestamp;
        loginHistory.ipAddress = ipAddress;
        loginHistory.userAgent = userAgent;
        loginHistory.successful = false;
        return loginHistory;
    }


}
