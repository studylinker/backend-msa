package com.study.auth.dto;

public class UserLoginVerifyResponse {

    private Long userId;
    private String username;
    private String role;   // "USER", "ADMIN" 등

    public UserLoginVerifyResponse() {}

    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getRole() { return role; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setRole(String role) { this.role = role; }
}