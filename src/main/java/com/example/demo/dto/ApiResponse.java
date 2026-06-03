package com.example.demo.dto;

public class ApiResponse {
    private String message;
    private boolean success;

    public ApiResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    // Gettery są wymagane, żeby Spring mógł stworzyć JSON-a!
    public String getMessage() {
        return message;
    }
    public boolean isSuccess() {
        return success;
    }
}