package com.juliy.store.backend.security.dto;

import java.time.LocalDateTime;

public record UserConnectionStatusResponse(
        String username,
        String role,
        boolean enabled,
        boolean connected,
        LocalDateTime lastConnectionAt
) {
}
