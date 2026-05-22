
package com.samora.authservice.app.auth;

import com.samora.authservice.app.auth.dto.BasicLoginRequest;
import com.samora.authservice.app.auth.dto.LoginResponse;
import com.samora.authservice.app.user.User;
import com.samora.authservice.app.user.UserRepository;
import com.samora.authservice.exception.BadRequestException;
import com.samora.authservice.exception.InvalidCredentialsException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;
    private final GroupPermissionRepository groupPermissionRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-days}")
    private int jwtExpirationDays;

    public AuthServiceImpl(
            UserRepository userRepository,
            UserGroupRepository userGroupRepository,
            GroupPermissionRepository groupPermissionRepository,
            PermissionRepository permissionRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.userGroupRepository = userGroupRepository;
        this.groupPermissionRepository = groupPermissionRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponse basicLogin(BasicLoginRequest request) {

        // 1. Resolve the user by whichever identifier was provided
        User user = resolveUser(request);

        // 2. Validate the password
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials and/or password.");
        }

        // 3. Resolve permissions
        List<String> permissions = resolvePermissions(user.getId());

        // 4. Generate access token
        String accessToken = generateAccessToken(user, permissions);

        // 5. Update lastLogin
        user.setLastLogin(OffsetDateTime.now());
        userRepository.save(user);

        return LoginResponse.from(user, permissions, accessToken);
    }

    // --- Private helpers ---

    private User resolveUser(BasicLoginRequest request) {
        boolean hasEmail = request.email() != null && !request.email().isBlank();
        boolean hasUsername = request.username() != null && !request.username().isBlank();
        boolean hasPhone = request.phoneNumber() != null && !request.phoneNumber().isBlank();

        long identifierCount = List.of(hasEmail, hasUsername, hasPhone)
                .stream().filter(b -> b).count();

        if (identifierCount == 0) {
            throw new BadRequestException("Provide one of: email, username, or phoneNumber.");
        }
        if (identifierCount > 1) {
            throw new BadRequestException("Provide only one identifier: email, username, or phoneNumber.");
        }

        Optional<User> found;

        if (hasEmail) {
            found = userRepository.findByEmail(request.email());
        } else if (hasUsername) {
            found = userRepository.findByUsername(request.username());
        } else {
            found = userRepository.findByPhoneNumber(request.phoneNumber());
        }

        return found.orElseThrow(() ->
                new InvalidCredentialsException("Invalid credentials and/or password.")
        );
    }

    private List<String> resolvePermissions(Long userId) {
        // Step 1 — get the user's group IDs
        List<Long> groupIds = userGroupRepository.findAllByUserId(userId)
                .stream()
                .map(userGroup -> userGroup.getGroup().getId())
                .toList();

        if (groupIds.isEmpty()) return List.of();

        // Step 2 — get permission IDs for those groups
        List<Long> permissionIds = groupPermissionRepository.findAllByGroupIdIn(groupIds)
                .stream()
                .map(gp -> gp.getPermission().getId())
                .toList();

        if (permissionIds.isEmpty()) return List.of();

        // Step 3 — fetch permission code names
        return permissionRepository.findAllByIdIn(permissionIds)
                .stream()
                .map(Permission::getCodeName)
                .toList();
    }

    private String generateAccessToken(User user, List<String> permissions) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        long expiryMs = (long) jwtExpirationDays * 24 * 60 * 60 * 1000;

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("userId", user.getId())
                .claim("username", user.getUsername())
                .claim("name", user.getName())
                .claim("email", user.getEmail())
                .claim("phoneNumber", user.getPhoneNumber())
                .claim("permissions", permissions)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(key)
                .compact();
    }
}