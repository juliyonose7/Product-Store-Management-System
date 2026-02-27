package com.juliy.store.backend.security.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    @Test
    void shouldGenerateAndValidateAccessAndRefreshTokens() {
        JwtService jwtService = new JwtService(SECRET, 3600000, 7200000);
        UserDetails user = User.withUsername("admin")
                .password("{noop}admin123")
                .roles("ADMIN")
                .build();

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        assertNotNull(accessToken);
        assertNotNull(refreshToken);
        assertEquals("admin", jwtService.extractUsername(accessToken));
        assertEquals("admin", jwtService.extractUsername(refreshToken));
        assertTrue(jwtService.isTokenValid(accessToken, user));
        assertTrue(jwtService.isRefreshTokenValid(refreshToken, user));
        assertFalse(jwtService.isTokenValid(refreshToken, user));
        assertFalse(jwtService.isRefreshTokenValid(accessToken, user));
        assertTrue(jwtService.extractExpirationEpochMs(accessToken) > System.currentTimeMillis());
    }
}
