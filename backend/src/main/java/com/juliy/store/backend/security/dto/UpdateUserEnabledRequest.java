package com.juliy.store.backend.security.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateUserEnabledRequest(
        @NotNull(message = "Enabled flag is required")
        Boolean enabled
) {
}
