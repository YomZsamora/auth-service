
package com.samora.authservice.app.auth;

import com.samora.authservice.app.auth.dto.BasicLoginRequest;
import com.samora.authservice.app.auth.dto.LoginResponse;
import com.samora.authservice.app.user.UserService;
import com.samora.authservice.app.user.dto.RegisterUserRequest;
import com.samora.authservice.app.user.dto.UserResponse;
import com.samora.authservice.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    public AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/basic-registration")
    public ResponseEntity<ApiResponse<UserResponse>> basicRegistration(
            @Valid @RequestBody RegisterUserRequest request) {

        UserResponse user = userService.registerUser(request);
        ApiResponse<UserResponse> response = ApiResponse.success(
                201,
                user.email() + " has been successfully registered.",
                user
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/basic-login")
    public ResponseEntity<ApiResponse<LoginResponse>> basicLogin(
            @Valid @RequestBody BasicLoginRequest request) {

        LoginResponse loginResponse = authService.basicLogin(request);
        ApiResponse<LoginResponse> response = ApiResponse.success(
                200,
                "Logged in successfully.",
                loginResponse
        );
        return ResponseEntity.ok(response);
    }
}