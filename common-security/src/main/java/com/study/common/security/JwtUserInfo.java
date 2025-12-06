package com.study.common.security;

public class JwtUserInfo {

    private final Long userId;
    private final String username;
    private final String role;   // ex: "USER", "ADMIN"

    public JwtUserInfo(Long userId, String username, String role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    // 관리자 여부 체크
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return "JwtUserInfo{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}