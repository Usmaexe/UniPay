package com.unipay.security;

import com.unipay.models.UserSession;
import com.unipay.service.session.UserSessionService;
import com.unipay.utils.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserSessionService userSessionService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = parseJwt(request);
        if (jwt != null) {
            try {
                log.debug("Processing JWT: {}...", jwt.substring(0, Math.min(jwt.length(), 10)));

                // 1. Validate signature, expiry, and userStatus
                if (!jwtService.validateToken(jwt)) {
                    log.warn("JWT failed validation");
                    reject(response, "Invalid or expired token");
                    return;
                }

                // 2. Verify session
                String sessionId = jwtService.getSessionIdFromToken(jwt);
                UserSession session = userSessionService.validateAndRefreshSession(sessionId);
                if (session == null) {
                    log.warn("Session {} invalid or expired", sessionId);
                    reject(response, "Session expired or revoked");
                    return;
                }

                // 3. Load UserDetailsImpl and set Authentication
                String username = jwtService.getUsernameFromToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);

                log.info("Authenticated session {} for user {}", sessionId, username);

            } catch (Exception ex) {
                log.error("JWT processing error", ex);
                reject(response, "Authentication failed");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response, String message) throws IOException {
        SecurityContextHolder.clearContext();
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
    }

    private String parseJwt(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}

