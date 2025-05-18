package com.unipay.security;


import com.unipay.exception.MfaVerificationRequiredException;
import com.unipay.exception.SessionExpiredException;
import com.unipay.payload.UserDetailsImpl;
import com.unipay.utils.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
@RequiredArgsConstructor
public class SessionValidationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                validateToken(token);
                processAuthentication(token, request);
            }
        }catch (JwtException e) {
            handleSecurityException(request, response,
                    new AuthenticationCredentialsNotFoundException("Invalid token", e));
        } catch (AuthenticationException e) {
            handleSecurityException(request, response, e);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/v1/auth/confirm") ||
                path.startsWith("/v1/auth/login") ||
                path.startsWith("/v1/auth/register") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs");
    }

    private void handleSecurityException(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        SecurityContextHolder.clearContext();
        authenticationEntryPoint.commence(request, response, exception);
    }

    private void validateToken(String token) {
        if (jwtService.isTokenExpired(token)) {
            throw new JwtException("Token expired");
        }
    }

    private void processAuthentication(String token, HttpServletRequest request) {
        String sessionId = jwtService.extractSessionId(token);
        String username = jwtService.extractUsername(token);

        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);

        validateSession(userDetails, sessionId);
        validateMfaState(userDetails, request);
        setSecurityContext(userDetails, request);
    }

    private void validateSession(UserDetailsImpl userDetails, String sessionId) {
        if (!userDetails.isSessionValid(sessionId)) {
            throw new SessionExpiredException();
        }
    }

    private void validateMfaState(UserDetailsImpl userDetails, HttpServletRequest request) {
        if (userDetails.isMfaRequired() &&
                !userDetails.isMfaVerified() &&
                requiresMfaVerification(request)) {
            throw new MfaVerificationRequiredException();
        }
    }

    private boolean requiresMfaVerification(HttpServletRequest request) {
        return !request.getRequestURI().contains("/mfa/verify");
    }

    private void setSecurityContext(UserDetailsImpl userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
    private void handleError(HttpServletResponse response,
                             int statusCode,
                             String message) throws IOException {
        // 1. Clear security context
        SecurityContextHolder.clearContext();

        // 2. Send standardized error response
        response.sendError(statusCode, message);

        // 3. Optional: Add security headers
        response.setHeader("WWW-Authenticate", "Bearer error=\"invalid_token\"");
    }
    private void processJwtToken(String jwt, HttpServletRequest request) {
        try {
            String username = jwtService.extractUsername(jwt);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    setSecurityContextAuthentication(userDetails, request);
                }
            }
        } catch (JwtException | UsernameNotFoundException e) {
            logger.error("JWT processing error", e);
        }
    }

    private void setSecurityContextAuthentication(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}
