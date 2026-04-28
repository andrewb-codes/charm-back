package ru.andrewb.charm.back.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.andrewb.charm.back.config.AppJwtProperties;
import ru.andrewb.charm.back.model.Role;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        AppJwtProperties properties = new AppJwtProperties();
        properties.setSecret("12345678901234567890123456789012");
        properties.setAccessTokenTtlMin(60L);
        jwtService = new JwtService(properties);
    }

    @Test
    void generateToken_shouldProduceValidTokenWithExpectedClaims() {
        AuthUser user = new AuthUser(7L, "user@mail.com", "hashed", Role.USER);

        String token = jwtService.generateToken(user);

        assertTrue(jwtService.isValid(token));
        assertEquals(7L, jwtService.extractUserId(token));
        assertEquals("user@mail.com", jwtService.extractEmail(token));
        assertEquals("USER", jwtService.extractRole(token));
        assertEquals(3600L, jwtService.getAccessTokenTtlSeconds());
    }

    @Test
    void isValid_shouldReturnFalseForGarbageToken() {
        assertFalse(jwtService.isValid("invalid.jwt.token"));
    }

    @Test
    void extractUserId_shouldThrowForInvalidToken() {
        assertThrows(Exception.class, () -> jwtService.extractUserId("invalid.jwt.token"));
    }
}

