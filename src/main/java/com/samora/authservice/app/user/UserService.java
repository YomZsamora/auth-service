
package com.samora.authservice.app.user;

import com.samora.authservice.app.user.dto.RegisterUserRequest;
import com.samora.authservice.app.user.dto.UserResponse;

public interface UserService {

    UserResponse registerUser(RegisterUserRequest request);

}