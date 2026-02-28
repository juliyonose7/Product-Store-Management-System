package com.juliy.store.backend.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FirstAccessPasswordRequest(
        @NotBlank String username,
        @NotBlank String temporaryPassword,
        @NotBlank @Size(min = 6, message = "New password must be at least 6 characters") String newPassword
) {
}
