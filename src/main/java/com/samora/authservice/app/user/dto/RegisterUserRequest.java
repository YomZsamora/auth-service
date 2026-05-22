
package com.samora.authservice.app.user.dto;

import jakarta.validation.constraints.*;

public record RegisterUserRequest(

        @NotBlank(message = "Username is required.")
        @Size(max = 255, message = "Username must not exceed 255 characters.")
        String username,

        @NotBlank(message = "Name is required.")
        @Size(max = 255, message = "Name must not exceed 255 characters.")
        String name,

        @NotBlank(message = "Phone number is required.")
        @Pattern(
                regexp = "^\\+[1-9]\\d{6,14}$",
                message = "Phone number must be a valid international format (e.g. +254712345678)."
        )
        String phoneNumber,

        @NotBlank(message = "Email is required.")
        @Email(message = "A valid email address is required.")
        @Pattern(
                regexp = "^[a-zA-Z0-9._%+\\-]+@(?!\\[)[a-zA-Z0-9\\-]+(\\.[a-zA-Z0-9\\-]+)*\\.[a-zA-Z]{2,6}$",
                message = "Email must have a valid domain and recognized top-level domain."
        )
        String email,

        @NotBlank(message = "Password is required.")
        @Size(min = 6, message = "Password must be at least 6 characters long.")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit."
        )
        String password,

        @NotBlank(message = "Password confirmation is required.")
        String passwordConfirm

) {}