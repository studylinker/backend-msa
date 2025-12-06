package com.study.user.dto;

import com.study.user.domain.User;

public class LoginVerifyResponse {

    private Long userId;
    private String username;
    private String role;  // "USER", "ADMIN"

    public LoginVerifyResponse() {}

    public LoginVerifyResponse(Long userId, String username, String role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public static LoginVerifyResponse fromUser(User user) {
        return new LoginVerifyResponse(
                user.getUserId(),
                user.getUsername(),
                user.getRole().name()
        );
    }

    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getRole() { return role; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setRole(String role) { this.role = role; }
}