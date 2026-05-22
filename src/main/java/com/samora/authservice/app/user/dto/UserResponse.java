
package com.samora.authservice.app.user.dto;

import com.samora.authservice.app.user.User;

import java.time.OffsetDateTime;

public record UserResponse(
        Long id,
        String username,
        String name,
        String email,
        String phoneNumber,
        OffsetDateTime lastLogin,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getLastLogin(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}