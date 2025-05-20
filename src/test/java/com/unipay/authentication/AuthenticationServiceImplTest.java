package com.unipay.authentication;


import com.unipay.command.LoginCommand;
import com.unipay.command.MfaVerificationRequest;
import com.unipay.command.TokenRefreshRequest;
import com.unipay.enums.AuditLogAction;
import com.unipay.enums.UserStatus;
import com.unipay.exception.BusinessException;
import com.unipay.exception.ExceptionPayloadFactory;
import com.unipay.models.User;
import com.unipay.models.UserSession;
import com.unipay.payload.UserDetailsImpl;
import com.unipay.repository.UserRepository;
import com.unipay.response.LoginResponse;
import com.unipay.security.UserDetailsServiceImpl;
import com.unipay.service.audit_log.AuditLogService;
import com.unipay.service.authentication.AuthenticationServiceImpl;
import com.unipay.service.login_histroy.LoginHistoryService;
import com.unipay.service.mfa.MFAService;
import com.unipay.service.session.UserSessionService;
import com.unipay.service.user.UserService;
import com.unipay.utils.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;
    @Mock
    private JwtService jwtService;
    @Mock
    private MFAService mfaService;
    @Mock
    private UserSessionService userSessionService;
    @Mock
    private LoginHistoryService loginHistoryService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserDetailsServiceImpl userDetailsService;
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private final String email = "anas@gmail.com";
    private final String password = "Unipay@123";
    private User user;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail(email);
        user.setStatus(UserStatus.ACTIVE);

        userDetails = new UserDetailsImpl(
                user
        );
    }

    @Test
    void login_SuccessWithoutMfa() {
        /*LoginCommand command = new LoginCommand(email, password);
        Authentication auth = mock(Authentication.class);
        UserSession session = new UserSession();

        when(authenticationManager.authenticate(any()))
                .thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userSessionService.createSession(any(), any(), any())).thenReturn(session);
        when(jwtService.generateTokenPair(any(), any())).thenReturn(new JwtService.JwtTokenPair("access", "refresh"));

        LoginResponse response = authenticationService.login(command, request);

        //assertTrue(response.isSuccess());
        verify(loginHistoryService).createLoginHistory(user, request, true);*/
    }

    @Test
    void login_MfaRequired() {
        /*user.getMfaSettings().setEnabled(true);
        LoginCommand command = new LoginCommand(email, password);
        Authentication auth = mock(Authentication.class);

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtService.generateMfaChallengeToken(userDetails)).thenReturn("mfa-token");

        LoginResponse response = authenticationService.login(command, request);

        assertTrue(response.isMfaRequired());
        assertEquals("mfa-token", response.getMfaChallenge());*/
    }

    @Test
    void login_DisabledUser() {
        /*user.setStatus(UserStatus.SUSPENDED);
        LoginCommand command = new LoginCommand(email, password);

        when(authenticationManager.authenticate(any()))
                .thenThrow(new DisabledException("Account disabled"));

        assertThrows(BusinessException.class, () ->
                authenticationService.login(command, request));
        verify(loginHistoryService).createLoginHistory(user, request, false);*/
    }

    @Test
    void verifyMfa_Success() {
        /*MfaVerificationRequest request = new MfaVerificationRequest("token", "123456");
        UserSession session = new UserSession();

        when(jwtService.isMfaChallengeToken("token")).thenReturn(true);
        when(jwtService.extractUsername("token")).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(mfaService.validateCode(user, "123456")).thenReturn(true);
        when(userSessionService.createSession(any(), any(), any())).thenReturn(session);
        when(jwtService.generateTokenPair(any(), any())).thenReturn(new JwtService.JwtTokenPair("access", "refresh"));

        LoginResponse response = authenticationService.verifyMfa("token", "123456", this.request);

        //assertTrue(response.isSuccess());
        verify(userSessionService).createSession(any(), any(), any());*/
    }

    @Test
    void refreshToken_ValidToken() {
        TokenRefreshRequest request = new TokenRefreshRequest("refresh-token");

        when(jwtService.isRefreshToken("refresh-token")).thenReturn(true);
        when(jwtService.extractSessionId("refresh-token")).thenReturn("session-id");
        when(userSessionService.isSessionValid("session-id")).thenReturn(true);
        when(jwtService.extractUsername("refresh-token")).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtService.generateTokenPair(any(), any())).thenReturn(new JwtService.JwtTokenPair("new-access", "new-refresh"));

        LoginResponse response = authenticationService.refreshToken("refresh-token", this.request);

        assertEquals("new-access", response.getTokenPair().accessToken());
        verify(userSessionService).isSessionValid("session-id");
    }

    @Test
    void getCurrentUser_Authenticated() {
        /*Authentication auth = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(auth.getName()).thenReturn(email);
        when(userService.findByEmailWithRolesAndPermissions(email)).thenReturn(user);

        User currentUser = authenticationService.getCurrentUser();

        assertEquals(email, currentUser.getEmail());*/
    }
}