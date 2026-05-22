
package com.samora.authservice.app.auth;

import com.samora.authservice.app.auth.dto.BasicLoginRequest;
import com.samora.authservice.app.auth.dto.LoginResponse;

public interface AuthService {

    LoginResponse basicLogin(BasicLoginRequest request);

}