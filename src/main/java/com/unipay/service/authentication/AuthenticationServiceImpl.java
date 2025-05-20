package com.unipay.service.authentication;

import com.unipay.annotation.Auditable;
import com.unipay.command.LoginCommand;
import com.unipay.command.UserRegisterCommand;
import com.unipay.enums.AuditLogAction;
import com.unipay.enums.UserStatus;
import com.unipay.exception.BusinessException;
import com.unipay.exception.ExceptionPayloadFactory;
import com.unipay.models.User;
import com.unipay.models.UserSession;
import com.unipay.payload.UserDetailsImpl;
import com.unipay.response.LoginResponse;
import com.unipay.security.UserDetailsServiceImpl;
import com.unipay.service.audit_log.AuditLogService;
import com.unipay.service.login_histroy.LoginHistoryService;
import com.unipay.service.mfa.MFAService;
import com.unipay.service.session.UserSessionService;
import com.unipay.service.user.UserService;
import com.unipay.utils.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final JwtService jwtService;
    private final MFAService mfaService;
    private final UserService userService;
    private final AuditLogService auditLogService;
    private final UserSessionService userSessionService;
    private final LoginHistoryService loginHistoryService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    @Auditable(action = "USER_REGISTRATION")
    public void register(UserRegisterCommand command, HttpServletRequest request) {
        userService.create(command, request);
    }

    @Override
    @Transactional
    @Auditable(action = "USER_LOGIN")
    public LoginResponse login(LoginCommand command, HttpServletRequest request) {
        try {
            Authentication authentication = authenticateUser(command);
            User user = getAuthenticatedUser(authentication);
            validateUserIsActive(user);

            if (isMfaEnabled(user)) {
                String challengeToken = generateMfaChallengeToken(authentication);
                return LoginResponse.mfaRequired(challengeToken);
            }
            userSessionService.revokeAllSessions(user);
            UserSession session = createUserSession(user, request);
            logLoginSuccess(user, request);
            return buildLoginResponse(authentication, session);

        } catch (AuthenticationException e) {
            return handleLoginException(command.getEmail(), request, e);
        }
    }

    @Override
    @Transactional
    @Auditable(action = "PASSWORD_RESET_REQUEST")
    public void forgotPassword(String email, HttpServletRequest request) {
        log.info("Forgot password requested for [{}]", email);
        userService.forgotPassword(email);
        auditLogService.createAuditLog(
                userService.findByEmailWithRolesAndPermissions(email),
                AuditLogAction.PASSWORD_RESET_REQUEST.getAction(),
                "Password reset requested"
        );
    }

    @Override
    @Transactional
    @Auditable(action = "MFA_VERIFICATION")
    public LoginResponse verifyMfa(String challengeToken, String code, HttpServletRequest request) {
        validateChallengeToken(challengeToken);

        String email = jwtService.extractUsername(challengeToken);
        User user = userService.findByEmail(email);

        if (!mfaService.validateCode(user, code)) {
            return handleLoginException(email, request, new BadCredentialsException("Invalid MFA code"));
        }

        UserSession session = createUserSession(user, request);
        logLoginSuccess(user, request);

        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(email);
        userDetails.setMfaVerified(true);

        return buildLoginResponse(userDetails, session);
    }

    @Override
    @Transactional
    @Auditable(action = "TOKEN_REFRESH")
    public LoginResponse refreshToken(String refreshToken, HttpServletRequest request) {
        try {
            validateRefreshToken(refreshToken);
            String sessionId = jwtService.extractSessionId(refreshToken);
            String email = jwtService.extractUsername(refreshToken);

            validateSession(sessionId);

            UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(email);
            return buildTokenResponse(userDetails, sessionId);

        } catch (JwtException | UsernameNotFoundException e) {
            throw new BusinessException(ExceptionPayloadFactory.INVALID_TOKEN.get());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication == null ? null : authentication.getName();
        if (email == null) {
            throw new BusinessException(ExceptionPayloadFactory.USER_NOT_AUTHENTICATED.get());
        }
        return userService.findByEmail(email);
    }


    @Override
    @Transactional
    @Auditable(action = "USER_LOGOUT")
    public void logout(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        String sessionId = jwtService.extractSessionId(token);
        userSessionService.invalidateSession(sessionId);

        User user = getCurrentUser();
        auditLogService.createAuditLog(user, AuditLogAction.LOGOUT.getAction(), "User logged out");
    }

    // ===== Helper Methods =====

    private Authentication authenticateUser(LoginCommand command) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(command.getEmail(), command.getPassword()));
    }

    private User getAuthenticatedUser(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userService.findByEmail(userDetails.getUsername());
    }

    private void validateUserIsActive(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new DisabledException("User account is not active");
        }
    }

    private boolean isMfaEnabled(User user) {
        return user.getMfaSettings() != null && user.getMfaSettings().isEnabled();
    }

    private String generateMfaChallengeToken(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return jwtService.generateMfaChallengeToken(userDetails);
    }

    private UserSession createUserSession(User user, HttpServletRequest request) {
        String deviceId = getDeviceName();
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = getClientIp(request);

        return userSessionService.createSession(user, deviceId, userAgent, ipAddress);
    }


    private String getDeviceName() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostName();
        } catch (UnknownHostException e) {
            System.err.println("Unable to determine device name.");
            e.printStackTrace();
            return "unknown-device";
        }
    }

    public static String getClientIp(HttpServletRequest request) {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostAddress();
        }catch (UnknownHostException e) {
            System.err.println("Unable to determine IP address.");
            e.printStackTrace();
            return "unknown-ipAddress";
        }
    }


    private LoginResponse buildLoginResponse(UserDetailsImpl userDetails, UserSession session) {
        return LoginResponse.success(
                jwtService.generateTokenPair(userDetails, session.getId()),
                userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList())
        );
    }

    private LoginResponse buildLoginResponse(Authentication authentication, UserSession session) {
        return buildLoginResponse((UserDetailsImpl) authentication.getPrincipal(), session);
    }

    private LoginResponse buildTokenResponse(UserDetailsImpl userDetails, String sessionId) {
        return LoginResponse.success(
                jwtService.generateTokenPair(userDetails, sessionId),
                userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList())
        );
    }

    private void validateChallengeToken(String token) {
        if (!jwtService.isMfaChallengeToken(token)) {
            throw new BusinessException(ExceptionPayloadFactory.INVALID_MFA_CHALLENGE.get());
        }
    }

    private void validateRefreshToken(String token) {
        if (!jwtService.isRefreshToken(token)) {
            throw new BusinessException(ExceptionPayloadFactory.INVALID_TOKEN.get());
        }
    }

    private void validateSession(String sessionId) {
        if (!userSessionService.isSessionValid(sessionId)) {
            throw new BusinessException(ExceptionPayloadFactory.INVALID_SESSION.get());
        }
    }

    private LoginResponse handleLoginException(String email, HttpServletRequest request, AuthenticationException e) {
        ExceptionPayloadFactory payload;

        if (e instanceof DisabledException) {
            payload = ExceptionPayloadFactory.USER_NOT_ACTIVE;
        } else if (e instanceof BadCredentialsException) {
            payload = ExceptionPayloadFactory.INVALID_PAYLOAD;
        } else {
            payload = ExceptionPayloadFactory.AUTHENTICATION_FAILED;
        }

        userService.findByEmailWithOptional(email).ifPresent(user -> {
            loginHistoryService.createLoginHistory(user, request, false);
            auditLogService.createAuditLog(user, AuditLogAction.LOGIN_FAILED.getAction(), "Failed login attempt: " + e.getMessage());
        });

        throw new BusinessException(payload.get());
    }
    private void logLoginSuccess(User user, HttpServletRequest request) {
        loginHistoryService.createLoginHistory(user, request, true);
        auditLogService.createAuditLog(user, AuditLogAction.LOGIN_SUCCESS.getAction(), "Successful login");
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(ExceptionPayloadFactory.INVALID_TOKEN.get());
        }
        return authHeader.substring(7);
    }
}
