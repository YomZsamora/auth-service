package com.samora.authservice.app.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record BasicLoginRequest(

        String email,

        String username,

        String phoneNumber,

        @NotBlank(message = "Password is required.")
        String password

) {}