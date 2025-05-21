package com.unipay.security;

import com.unipay.utils.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Checks whether an incoming request requires MFA and whether the user has completed MFA.
 */
@Component
public class CustomAuthorizationService {
    private final JwtService jwtService;

    public CustomAuthorizationService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Returns true if the request requires MFA and the JWT does not include a verified flag.
     */
    public boolean requiresMfaAndVerified(Authentication authentication, HttpServletRequest request) {
        String path = request.getRequestURI();
        String token = extractToken(request);

        // Skip MFA for public/auth/docs endpoints
        if (isPublicEndpoint(path)) {
            return false;
        }

        // If token missing or invalid, skip (another filter handles auth)
        if (token == null || !jwtService.validateToken(token)) {
            return false;
        }

        // Skip MFA check if user has just registered and MFA is not enabled
        Boolean mfaEnabled = jwtService.extractClaim(token, claims -> claims.get("mfaEnabled", Boolean.class));
        if (mfaEnabled == null || !mfaEnabled) {
            return false;
        }

        // If they reach here, MFA is enabled; check if it's verified
        Boolean mfaVerified = jwtService.extractClaim(token, claims -> claims.get("mfaVerified", Boolean.class));
        return mfaVerified == null || !mfaVerified;
    }

    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/v1/auth") ||
                path.startsWith("/api/v1/auth") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.contains("/mfa") ||
                path.equals("/v1/auth/login") ||
                path.equals("/v1/auth/register") ||
                path.equals("/v1/auth/confirm") ||
                path.equals("/v1/auth/current");
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.isNotEmpty(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
