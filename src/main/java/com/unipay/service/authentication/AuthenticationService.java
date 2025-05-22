package com.unipay.service.authentication;

import com.unipay.command.LoginCommand;
import com.unipay.command.UserRegisterCommand;
import com.unipay.models.User;
import com.unipay.response.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Service interface for handling authentication-related operations.
 * This includes user registration, login, MFA verification, and token refresh.
 */
public interface AuthenticationService {

    /**
     * Registers a new user in the system.
     *
     * @param command The registration command containing the user data.
     */
    void register(UserRegisterCommand command);

    /**
     * Logs a user in based on the provided login command.
     *
     * @param command The login command containing user credentials.
     * @return The login response containing the token and user roles.
     */
    LoginResponse login(LoginCommand command, HttpServletRequest request);

    /**
     * Retrieves the current authenticated user from the security context.
     *
     * @return The current authenticated user.
     */
    User getCurrentUser();
    String getCurrentEmail();
}
