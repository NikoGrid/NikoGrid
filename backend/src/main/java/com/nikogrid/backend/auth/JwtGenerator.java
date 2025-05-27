package com.nikogrid.backend.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtGenerator {

    private final SecretKey key;
    private final int expirationSeconds;

    @Autowired
    public JwtGenerator(@Value("${jwt.secret}") String secretKey, @Value("${jwt.expiration-seconds}") int expirationSeconds) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.expirationSeconds = expirationSeconds;
    }

    public JwtToken generateToken(String email) {
        final var currentDate = new Date();
        final var expireDate = new Date(currentDate.getTime() + expirationSeconds * 1000L);
        final var token = Jwts.builder()
                .subject(email)
                .issuedAt(currentDate)
                .expiration(expireDate)
                .signWith(key)
                .compact();

        return new JwtToken(token, expirationSeconds);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }
}
