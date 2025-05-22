package com.unipay.utils;

import com.unipay.enums.UserStatus;
import com.unipay.payload.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    /**
     * -- GETTER --
     *
     * @return the configured JWT expiration interval, in milliseconds
     */
    @Getter
    @Value("${jwt.expiration-ms}")
    private int expirationMs;

    private Key key() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateJwtToken(Authentication authentication, String sessionId) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .claim("sessionId", sessionId)
                .claim("authorities", userPrincipal.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .claim("userStatus", userPrincipal.getUser().getStatus().name())
                .setIssuer("UniPay")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
    }


    public String getUsernameFromToken(String token) {
        return parseToken(token).getBody().getSubject();
    }
    public boolean validateToken(String authToken) {
        try {
            Claims claims = parseToken(authToken).getBody();

            // Ensure token not expired by JJWT itself
            Date expiration = claims.getExpiration();
            if (expiration.before(new Date())) {
                log.warn("Expired JWT token: expiration {}", expiration);
                return false;
            }

            // Safely extract userStatus claim
            Object statusObj = claims.get("userStatus");
            if (!(statusObj instanceof String statusStr)) {
                log.warn("Missing or invalid 'userStatus' claim: {}", statusObj);
                return false;
            }

            try {
                UserStatus tokenStatus = UserStatus.valueOf(statusStr);
                if (tokenStatus != UserStatus.ACTIVE) {
                    log.warn("Token created with non-active status: {}", tokenStatus);
                    return false;
                }
            } catch (IllegalArgumentException iae) {
                log.warn("Unknown userStatus value in token: {}", statusStr);
                return false;
            }

            return true;

        } catch (ExpiredJwtException ex) {
            log.warn("Expired JWT token: {}", ex.getMessage());
        } catch (JwtException | IllegalArgumentException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        }
        return false;
    }

    private Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token);
    }

    public Date getExpirationFromToken(String token) {
        return parseToken(token).getBody().getExpiration();
    }

    public List<String> getAuthoritiesFromToken(String token) {
        return parseToken(token).getBody().get("authorities", List.class);
    }
    /**
     * Pulls the session‐ID claim out of the JWT.
     */
    public String getSessionIdFromToken(String token) {
        return parseToken(token)
                .getBody()
                .get("sessionId", String.class);
    }
}