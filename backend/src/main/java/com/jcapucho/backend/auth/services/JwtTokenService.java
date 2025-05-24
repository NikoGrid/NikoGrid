package com.jcapucho.backend.auth.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.jcapucho.backend.auth.entities.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class JwtTokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    public String generateToken(UserDetailsImpl user) {
        try{
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("auth-api")
                    .withIssuedAt(creationDate())
                    .withExpiresAt(generateExpirationTime())
                    .withSubject(user.getUsername())
                    .sign(algorithm);
        } catch (JWTCreationException e) {
            throw new RuntimeException("Error while generating token", e);
        }
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("auth-api")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException e) {
            return "";
        }
    }

    private Instant creationDate() {
        return ZonedDateTime.now(ZoneId.of("Europe/Lisbon")).toInstant();
    }

    private Instant generateExpirationTime() {
        return ZonedDateTime.now(ZoneId.of("Europe/Lisbon")).plusHours(1).toInstant();
    }
}
