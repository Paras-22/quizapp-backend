package com.example.quizapp.dto;

public class AuthResponse {
    // Here I add fields to hold JWT token, username, and role
    private String token;
    private String username;
    private String role;

    // Here I add constructor to initialize all fields
    public AuthResponse(String token, String username, String role) {
        this.token = token;
        this.username = username;
        this.role = role;
    }

    // Here I add getter for token
    public String getToken() {
        return token;
    }

    // Here I add getter for username
    public String getUsername() {
        return username;
    }

    // Here I add getter for role
    public String getRole() {
        return role;
    }
}
