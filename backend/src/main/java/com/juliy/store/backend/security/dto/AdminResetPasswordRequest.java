package com.juliy.store.backend.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminResetPasswordRequest(
        @NotBlank(message = "Temporary password is required")
        @Size(min = 6, message = "Temporary password must be at least 6 characters")
        String temporaryPassword
) {
}
