package com.samora.authservice.app.auth.dto;

import com.samora.authservice.app.user.User;

import java.time.OffsetDateTime;
import java.util.List;

public record LoginResponse(
        Long id,
        String username,
        String name,
        String email,
        String phoneNumber,
        OffsetDateTime lastLogin,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<String> permissions,
        String accessToken
) {
    public static LoginResponse from(User user, List<String> permissions, String accessToken) {
        return new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getLastLogin(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                permissions,
                accessToken
        );
    }
}