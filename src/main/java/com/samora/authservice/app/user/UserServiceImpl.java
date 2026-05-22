
package com.samora.authservice.app.user;

import com.samora.authservice.app.user.dto.RegisterUserRequest;
import com.samora.authservice.app.user.dto.UserResponse;
import com.samora.authservice.exception.ConflictException;
import com.samora.authservice.exception.PasswordMismatchException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse registerUser(RegisterUserRequest request) {

        if (!request.password().equals(request.passwordConfirm())) {
            throw new PasswordMismatchException("Passwords do not match.");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("A user with this email already exists.");
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("A user with this username already exists.");
        }

        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new ConflictException("A user with this phone number already exists.");
        }

        User user = User.builder()
                .username(request.username())
                .name(request.name())
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .password(passwordEncoder.encode(request.password()))
                .build();

        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
    }
}