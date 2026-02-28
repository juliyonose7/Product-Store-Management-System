package com.juliy.store.backend.security.dto;

public record UsersSummaryResponse(
        long totalUsers,
        long connectedUsers
) {
}
