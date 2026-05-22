
package com.samora.authservice.app.auth;

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
    public AuthController(UserService userService) {
        this.userService = userService;
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
}