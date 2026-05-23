package com.samora.authservice.app.user;

import com.samora.authservice.app.user.dto.RegisterUserRequest;
import com.samora.authservice.app.user.dto.UserResponse;

public interface UserService {

    // Registers a new user based on the provided registration request and returns the created user's details.
    UserResponse registerUser(RegisterUserRequest request);

}