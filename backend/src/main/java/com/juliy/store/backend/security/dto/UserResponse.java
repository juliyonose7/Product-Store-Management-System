package com.juliy.store.backend.security.dto;

public record UserResponse(
        String username,
        String role,
        boolean enabled,
        boolean mustChangePassword
) {
}
