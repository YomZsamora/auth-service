package com.samora.authservice.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samora.authservice.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Invoked when an unauthenticated request hits a protected endpoint.
     * Returns 401 — authentication credentials were missing or invalid.
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                "Authentication credentials were not provided.");
    }

    /**
     * Invoked when an authenticated user lacks the required permissions.
     * Returns 403 — the user is known but not allowed.
     */
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        writeErrorResponse(response, HttpServletResponse.SC_FORBIDDEN,
                "You do not have the required permission to perform this action.");
    }

    private void writeErrorResponse(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Object> apiResponse = ApiResponse.error(status, message, null);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
