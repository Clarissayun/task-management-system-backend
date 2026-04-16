package com.clarissa.task_management_system_backend.config;

import com.clarissa.task_management_system_backend.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final SecretKey accessTokenSecretKey;
    private final SecretKey refreshTokenSecretKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtTokenProvider(
            @Value("${app.jwt.access-token-secret}") String accessTokenSecret,
            @Value("${app.jwt.refresh-token-secret}") String refreshTokenSecret,
            @Value("${app.jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
            @Value("${app.jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs) {
        this.accessTokenSecretKey = Keys.hmacShaKeyFor(accessTokenSecret.getBytes(StandardCharsets.UTF_8));
        this.refreshTokenSecretKey = Keys.hmacShaKeyFor(refreshTokenSecret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String generateAccessToken(User user) {
        return buildToken(user, ACCESS_TOKEN_TYPE, accessTokenExpirationMs, accessTokenSecretKey);
    }

    public String generateRefreshToken(User user) {
        return buildToken(user, REFRESH_TOKEN_TYPE, refreshTokenExpirationMs, refreshTokenSecretKey);
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token, ACCESS_TOKEN_TYPE, accessTokenSecretKey);
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token, REFRESH_TOKEN_TYPE, refreshTokenSecretKey);
    }

    public String getUserIdFromAccessToken(String token) {
        return parseClaims(token, accessTokenSecretKey).getSubject();
    }

    public String getUserIdFromRefreshToken(String token) {
        return parseClaims(token, refreshTokenSecretKey).getSubject();
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }

    private String buildToken(User user, String tokenType, long expirationMs, SecretKey secretKey) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(user.getId())
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    private boolean validateToken(String token, String expectedType, SecretKey secretKey) {
        try {
            Claims claims = parseClaims(token, secretKey);
            String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
            return expectedType.equals(tokenType);
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private Claims parseClaims(String token, SecretKey secretKey) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
