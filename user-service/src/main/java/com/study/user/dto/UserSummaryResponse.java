package com.study.user.dto;

public class UserSummaryResponse {

    private Long userId;
    private String username;
    private String name;

    public UserSummaryResponse(Long userId, String username, String name) {
        this.userId = userId;
        this.username = username;
        this.name = name;
    }

    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getName() { return name; }
}