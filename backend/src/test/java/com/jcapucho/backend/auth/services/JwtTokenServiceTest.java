package com.jcapucho.backend.auth.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.jcapucho.backend.auth.entities.User;
import com.jcapucho.backend.auth.entities.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.*;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenServiceTest {

    @InjectMocks
    private JwtTokenService jwtTokenService;

    private UserDetailsImpl userDetails;
    private User user;
    private final String testSecret = "my-jwt-secret";
    private final String testEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtTokenService, "secret", testSecret);

        user = User.builder()
                .email(testEmail)
                .password("password")
                .created_at(LocalDateTime.now())
                .roles(new ArrayList<>())
                .build();

        userDetails = new UserDetailsImpl(user);
    }

    @Test
    void generateToken_ShouldGenerateValidToken_WhenUserDetailsProvided() {
        String token = jwtTokenService.generateToken(userDetails);

        assertNotNull(token);
        DecodedJWT jwt = JWT.decode(token);
        assertEquals("auth-api", jwt.getIssuer());
        assertEquals(testEmail, jwt.getSubject());
        assertNotNull(jwt.getIssuedAt());
        assertNotNull(jwt.getExpiresAt());

        String validated = jwtTokenService.validateToken(token);
        assertEquals(testEmail, validated);
    }


    @Test
    void generateToken_ShouldThrowRuntimeException_WhenJWTCreationFails() {
        ReflectionTestUtils.setField(jwtTokenService, "secret", testSecret); // valid secret

        try (MockedStatic<JWT> mockedJWT = mockStatic(JWT.class)) {
            mockedJWT.when(JWT::create).thenThrow(new JWTCreationException("Simulated failure", null));

            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    jwtTokenService.generateToken(userDetails));

            assertEquals("Error while generating token", exception.getMessage());
            assertInstanceOf(JWTCreationException.class, exception.getCause());
        }
    }

    @Test
    void validateToken_ShouldReturnUsername_WhenTokenIsValid() {
        String validToken = generateValidToken();

        String result = jwtTokenService.validateToken(validToken);

        assertEquals(testEmail, result);
    }

    @Test
    void validateToken_ShouldReturnEmptyString_WhenTokenIsInvalid() {
        String invalidToken = "invalid.jwt.token";

        String result = jwtTokenService.validateToken(invalidToken);

        assertEquals("", result);
    }

    @Test
    void validateToken_ShouldReturnEmptyString_WhenTokenIsExpired() {
        ZonedDateTime pastTime = ZonedDateTime.of(2020, 1, 1, 12, 0, 0, 0, ZoneId.of("Europe/Lisbon"));
        String expiredToken = generateTokenWithCustomTime(pastTime);

        String result = jwtTokenService.validateToken(expiredToken);

        assertEquals("", result);
    }

    @Test
    void validateToken_ShouldReturnEmptyString_WhenTokenHasWrongIssuer() {
        Algorithm algorithm = Algorithm.HMAC256(testSecret);
        String tokenWithWrongIssuer = JWT.create()
                .withIssuer("wrong-issuer")
                .withSubject(testEmail)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plusSeconds(3600))
                .sign(algorithm);

        String result = jwtTokenService.validateToken(tokenWithWrongIssuer);

        assertEquals("", result);
    }

    @Test
    void validateToken_ShouldReturnEmptyString_WhenTokenSignedWithDifferentSecret() {
        Algorithm differentAlgorithm = Algorithm.HMAC256("different-secret");
        String tokenWithDifferentSecret = JWT.create()
                .withIssuer("auth-api")
                .withSubject(testEmail)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plusSeconds(3600))
                .sign(differentAlgorithm);

        String result = jwtTokenService.validateToken(tokenWithDifferentSecret);

        assertEquals("", result);
    }

    @Test
    void validateToken_ShouldReturnEmptyString_WhenTokenIsNull() {
        String result = jwtTokenService.validateToken(null);

        assertEquals("", result);
    }

    @Test
    void validateToken_ShouldReturnEmptyString_WhenTokenIsEmpty() {
        String result = jwtTokenService.validateToken("");

        assertEquals("", result);
    }

    @Test
    void generateToken_ShouldUseCorrectTimeZone() {
        ZonedDateTime fixedZdt = ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneId.of("Europe/Lisbon"));
        Instant expectedIssuedAt = fixedZdt.toInstant();
        Instant expectedExpiresAt = fixedZdt.plusHours(1).toInstant();

        try (MockedStatic<ZonedDateTime> mockedZonedDateTime = mockStatic(ZonedDateTime.class, CALLS_REAL_METHODS)) {
            mockedZonedDateTime.when(() -> ZonedDateTime.now(ZoneId.of("Europe/Lisbon")))
                    .thenReturn(fixedZdt);

            String token = jwtTokenService.generateToken(userDetails);

            DecodedJWT decodedJWT = JWT.decode(token);

            assertEquals(expectedIssuedAt, decodedJWT.getIssuedAt().toInstant());
            assertEquals(expectedExpiresAt, decodedJWT.getExpiresAt().toInstant());
        }
    }


    @Test
    void generateToken_ShouldSetExpirationTimeOneHourAfterCreation() {
        ZonedDateTime creationTime = ZonedDateTime.of(2024, 3, 15, 10, 0, 0, 0, ZoneId.of("Europe/Lisbon"));

        try (MockedStatic<ZonedDateTime> mockedZonedDateTime = mockStatic(ZonedDateTime.class, CALLS_REAL_METHODS)) {
            mockedZonedDateTime.when(() -> ZonedDateTime.now(ZoneId.of("Europe/Lisbon")))
                    .thenReturn(creationTime);

            String token = jwtTokenService.generateToken(userDetails);

            DecodedJWT decodedJWT = JWT.decode(token);
            Instant issuedAt = decodedJWT.getIssuedAt().toInstant();
            Instant expiresAt = decodedJWT.getExpiresAt().toInstant();

            long durationInSeconds = expiresAt.getEpochSecond() - issuedAt.getEpochSecond();
            assertEquals(3600, durationInSeconds);
        }
    }


    @Test
    void generateToken_ShouldHandleUserWithNullRoles() {
        User userWithNullRoles = User.builder()
                .email(testEmail)
                .password("password")
                .roles(null)
                .build();
        UserDetailsImpl userDetailsWithNullRoles = new UserDetailsImpl(userWithNullRoles);

        assertDoesNotThrow(() -> jwtTokenService.generateToken(userDetailsWithNullRoles));
    }

    @Test
    void validateToken_ShouldWorkWithRecentlyGeneratedToken() {
        String freshToken = jwtTokenService.generateToken(userDetails);

        String result = jwtTokenService.validateToken(freshToken);

        assertEquals(testEmail, result);
    }

    private String generateValidToken() {
        Algorithm algorithm = Algorithm.HMAC256(testSecret);
        return JWT.create()
                .withIssuer("auth-api")
                .withSubject(testEmail)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plusSeconds(3600))
                .sign(algorithm);
    }

    private String generateTokenWithCustomTime(ZonedDateTime time) {
        Algorithm algorithm = Algorithm.HMAC256(testSecret);
        return JWT.create()
                .withIssuer("auth-api")
                .withSubject(testEmail)
                .withIssuedAt(time.toInstant())
                .withExpiresAt(time.plusHours(1).toInstant())
                .sign(algorithm);
    }
}