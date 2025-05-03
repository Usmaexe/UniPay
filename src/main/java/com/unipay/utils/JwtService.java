package com.unipay.utils;

import com.unipay.payload.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private static final String TOKEN_TYPE = "JWT";
    private static final String TOKEN_ISSUER = "Unipay-Auth-Service";

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${jwt.mfa-challenge-expiration}")
    private long mfaChallengeExpiration;

    public JwtTokenPair generateTokenPair(UserDetailsImpl userDetails, String sessionId) {
        String accessToken = buildAccessToken(userDetails, sessionId);
        String refreshToken = buildRefreshToken(userDetails, sessionId);
        return new JwtTokenPair(accessToken, refreshToken);
    }
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.containsKey("refresh") && claims.get("refresh", Boolean.class);
        } catch (JwtException e) {
            return false;
        }
    }

    public String generateMfaChallengeToken(UserDetailsImpl userDetails) {
        return Jwts.builder()
                .setHeaderParam("typ", TOKEN_TYPE)
                .setIssuer(TOKEN_ISSUER)
                .setSubject(userDetails.getUsername())
                .claim("mfaChallenge", true)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + mfaChallengeExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private String buildAccessToken(UserDetailsImpl userDetails, String sessionId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList());
        claims.put("sessionId", sessionId);
        claims.put("mfaEnabled", userDetails.isMfaRequired());

        return buildToken(claims, userDetails, accessTokenExpiration);
    }

    private String buildRefreshToken(UserDetailsImpl userDetails, String sessionId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sessionId", sessionId);
        claims.put("refresh", true);
        return buildToken(claims, userDetails, refreshTokenExpiration);
    }

    private String buildToken(Map<String, Object> claims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .setHeaderParam("typ", TOKEN_TYPE)
                .setIssuer(TOKEN_ISSUER)
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractSessionId(String token) {
        return extractClaim(token, claims -> claims.get("sessionId", String.class));
    }

    public boolean isMfaChallengeToken(String token) {
        return extractClaim(token, claims -> claims.get("mfaChallenge", Boolean.class)) != null;
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername())
                    && !isTokenExpired(token)
                    && validateSession(token);
        } catch (JwtException e) {
            return false;
        }
    }

    private boolean validateSession(String token) {
        String sessionId = extractSessionId(token);
        // Add session validation logic here (check against session repository)
        return sessionId != null; // Simplified for example
    }

    // Existing helper methods remain the same
    public String extractUsername(String token) throws JwtException {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) throws JwtException {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isMfaVerified(String token) {
        return extractClaim(token, claims -> claims.get("mfaVerified", Boolean.class));
    }

    public record JwtTokenPair(String accessToken, String refreshToken) {}
}