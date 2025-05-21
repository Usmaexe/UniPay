package com.unipay.security;

import com.unipay.payload.UserDetailsImpl;
import com.unipay.service.mfa.MFAService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class WebSecurity {

    private final MFAService mfaService;

    public WebSecurity(MFAService mfaService) {
        this.mfaService = mfaService;
    }

    public boolean checkAccess(Authentication authentication,
                               String userId,
                               String httpMethod,
                               String requestUri) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Path variable validation
        if (userId != null && !userDetails.getId().equals(userId)) {
            return false;
        }

        // MFA verification
        if (requiresMfaVerification(requestUri)) {
            if (!userDetails.isMfaVerified()) {
                return false;
            }
        }

        // Dynamic permission check
        return hasRequiredPermissions(userDetails, httpMethod, requestUri);
    }

    private boolean requiresMfaVerification(String requestUri) {
        return !requestUri.contains("/mfa/verify") &&
                !requestUri.contains("/mfa/qrcode");
    }

    private boolean hasRequiredPermissions(UserDetailsImpl userDetails,
                                           String httpMethod,
                                           String requestUri) {

        Set<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // Map request to required permissions
        Set<String> requiredPermissions = resolveRequiredPermissions(httpMethod, requestUri);

        return authorities.containsAll(requiredPermissions);
    }

    private Set<String> resolveRequiredPermissions(String httpMethod, String requestUri) {
        // Implement your permission mapping logic here
        // Example: Map REST methods to CRUD operations
        Map<String, String> methodMap = Map.of(
                "GET", "READ",
                "POST", "CREATE",
                "PUT", "UPDATE",
                "DELETE", "DELETE"
        );

        String resource = extractResourceFromUri(requestUri);
        String operation = methodMap.getOrDefault(httpMethod, "UNKNOWN");

        return Set.of(resource + "_" + operation);
    }

    private String extractResourceFromUri(String uri) {
        // Extract resource from URI pattern
        if (uri.startsWith("/v1/users")) return "USER";
        if (uri.startsWith("/v1/businesses")) return "BUSINESS";
        if (uri.startsWith("/v1/transactions")) return "TRANSACTION";
        return "DEFAULT_RESOURCE";
    }
    public boolean isOwner(Authentication authentication, String userId) {
        if (authentication == null || !authentication.isAuthenticated()) return false;
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId().equals(userId);
    }

    public boolean hasMfaEnabled(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return false;
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.isMfaRequired() && userDetails.isMfaVerified();
    }

    public boolean checkPermission(Authentication authentication, String resource, String action) {
        if (authentication == null || !authentication.isAuthenticated()) return false;
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String requiredPermission = resource.toUpperCase() + "_" + action.toUpperCase();
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(requiredPermission));
    }

    public boolean hasAnyPermissions(Authentication authentication, String... permissions) {
        if (authentication == null || !authentication.isAuthenticated()) return false;
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Set<String> userPermissions = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return Arrays.stream(permissions)
                .anyMatch(userPermissions::contains);
    }
}