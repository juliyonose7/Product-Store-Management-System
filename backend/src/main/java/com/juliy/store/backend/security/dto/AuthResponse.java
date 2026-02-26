package com.juliy.store.backend.security.dto;

public record AuthResponse(
        String token,
        String tokenType,
        String username,
        String role
) {
}
