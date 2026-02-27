package com.juliy.store.backend.security.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        String username,
        String role,
        long accessTokenExpiresAt,
        long refreshTokenExpiresAt
) {
}

