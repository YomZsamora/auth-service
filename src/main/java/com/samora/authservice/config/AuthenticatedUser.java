
package com.samora.authservice.config;

import java.util.List;

public record AuthenticatedUser(
        Long userId,
        String username,
        String name,
        String email,
        String phoneNumber,
        List<String> permissions
) {}