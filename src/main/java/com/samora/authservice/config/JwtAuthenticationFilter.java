
package com.samora.authservice.config;

import com.samora.authservice.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // No token — continue without setting authentication.
        // Spring Security will reject the request if the endpoint requires auth.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7); // strip "Bearer "

        if (!jwtUtils.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        Claims claims = jwtUtils.extractClaims(token);

        // Build the principal — equivalent to req.currentUser in Express
        List<String> permissions = claims.get("permissions", List.class);

        AuthenticatedUser principal = new AuthenticatedUser(
                claims.get("userId", Long.class),
                claims.get("username", String.class),
                claims.get("name", String.class),
                claims.get("email", String.class),
                claims.get("phoneNumber", String.class),
                permissions != null ? permissions : List.of()
        );

        // Map permissions to GrantedAuthority — Spring's permission representation
        List<SimpleGrantedAuthority> authorities = principal.permissions()
                .stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        // Populate the SecurityContext — equivalent to req.currentUser = { ... }
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}