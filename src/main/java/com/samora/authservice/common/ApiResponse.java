package com.samora.authservice.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.ALWAYS) // always serialize `data` even when null
@JsonPropertyOrder({ "code", "status", "message", "data" })
public class ApiResponse<T> {

    private int code;
    private String status;
    private String message;
    private T data;

    private ApiResponse(int code, String status, String message, T data) {
        this.code = code;
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // --- Factory methods ---

    public static <T> ApiResponse<T> success(int code, String message, T data) {
        return new ApiResponse<>(code, "success", message, data);
    }

    public static <T> ApiResponse<T> success(int code, String message) {
        return new ApiResponse<>(code, "success", message, null);
    }

    public static <T> ApiResponse<T> error(int code, String message, T data) {
        return new ApiResponse<>(code, "error", message, data);
    }

    // --- Getters (required for Jackson serialization) ---

    public int getCode() { return code; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}
