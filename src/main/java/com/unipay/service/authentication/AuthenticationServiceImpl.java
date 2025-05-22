package com.unipay.service.authentication;

import com.unipay.annotation.Auditable;
import com.unipay.command.LoginCommand;
import com.unipay.command.UserRegisterCommand;
import com.unipay.enums.UserStatus;
import com.unipay.exception.AuthException;
import com.unipay.models.LoginHistory;
import com.unipay.models.User;
import com.unipay.models.UserSession;
import com.unipay.payload.UserDetailsImpl;
import com.unipay.response.LoginResponse;
import com.unipay.service.mail.EmailService;
import com.unipay.service.session.UserSessionService;
import com.unipay.service.user.UserService;
import com.unipay.utils.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final EmailService emailService;
    private final JwtService jwtService;
    private final UserService userService;
    private final UserSessionService userSessionService;
    private final AuthenticationManager authenticationManager;



    @Override
    @Auditable(action = "USER_REGISTRATION")
    public void register(UserRegisterCommand command) {
        userService.create(command);
    }
    @Override
    @Transactional
    @Auditable(action = "USER_LOGIN")
    public LoginResponse login(LoginCommand command, HttpServletRequest request) {
        String email = command.getEmail();
        String password = command.getPassword();
        String userAgent = request.getHeader("User-Agent");
        String clientIp = getClientIp(request);
        String deviceId = getDeviceName();

        log.debug("Attempting login for email: {}", email);

        User user = retrieveUser(email);
        validateUserStatus(user, clientIp, userAgent);

        Authentication authentication = authenticateUser(email, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserSession session = manageSession(user, deviceId, clientIp, userAgent, request);
        String jwt = jwtService.generateJwtToken(authentication, session.getId());

        recordLoginSuccess(user, clientIp, userAgent);

        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        return new LoginResponse(jwt, principal.getUsername(), principal.getAuthorities());
    }
    @Transactional(readOnly = true)
    @Override
    public User getCurrentUser() {
        UserDetailsImpl principal = getCurrentUserPrincipal();
        return userService.findByEmailWithNoOptional(principal.getUsername());
    }
    @Override
    public String getCurrentEmail() {
        return getCurrentUserPrincipal().getUsername();
    }
    private UserDetailsImpl getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("No authenticated user found");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetailsImpl)) {
            throw new SecurityException("Invalid authentication principal type. Found: " +
                    principal.getClass().getName());
        }
        return (UserDetailsImpl) principal;
    }
    private Authentication authenticateUser(String email, String password) {
        try {
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (BadCredentialsException ex) {
            log.warn("Invalid login attempt for email: {}", email);
            throw new AuthException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }
    }

    private UserSession manageSession(User user, String deviceId, String clientIp, String userAgent, HttpServletRequest request) {
        boolean isNewDevice = !userSessionService.hasActiveSessionForDevice(user, deviceId);
        if (isNewDevice) {
            UserSession session = userSessionService.createSession(user, deviceId, clientIp, userAgent, Instant.now().plusMillis(jwtService.getExpirationMs()));
            emailService.sendNewLoginDetected(user.getEmail(), user.getUsername(), deviceId, request);
            return session;
        } else {
            return userSessionService.findActiveByUserAndDevice(user, deviceId, Instant.now())
                    .orElseThrow(() -> new IllegalStateException("Expected existing session"));
        }
    }
    private void recordLoginSuccess(User user, String clientIp, String userAgent) {
        user.addLoginHistory(LoginHistory.createSuccess(user, LocalDateTime.now(), clientIp, userAgent));
    }

    private String getDeviceName() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostName();
        } catch (UnknownHostException e) {
            log.error("Unable to determine device name.", e);
            return "unknown-device";
        }
    }

    public static String getClientIp(HttpServletRequest request) {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostAddress();
        } catch (UnknownHostException e) {
            log.error("Unable to determine IP address.", e);
            return "unknown-ipAddress";
        }
    }
    private User retrieveUser(String email) {
        return userService.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Login attempt for non-existent email: {}", email);
                    throw new BadCredentialsException("Invalid credentials");
                });
    }

    private void validateUserStatus(User user, String clientIp, String userAgent) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("Login attempt for disabled account. Email: {}, Status: {}", user.getEmail(), user.getStatus());
            user.addLoginHistory(LoginHistory.createFailure(user, LocalDateTime.now(), clientIp, userAgent, "ACCOUNT_DISABLED"));
            throw new AuthException("Account is not active", HttpStatus.UNAUTHORIZED);
        }
    }
}
