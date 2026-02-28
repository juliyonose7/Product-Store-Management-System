package com.juliy.store.backend.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "Username is required")
        @Size(max = 50, message = "Username must have at most 50 characters")
        String username,

        @NotBlank(message = "Role is required")
        String role,

        @NotBlank(message = "Temporary password is required")
        @Size(min = 6, message = "Temporary password must be at least 6 characters")
        String temporaryPassword
) {
}
