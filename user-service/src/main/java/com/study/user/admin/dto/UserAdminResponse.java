package com.study.user.admin.dto;

import com.study.user.domain.Role;
import com.study.user.domain.User;
import com.study.user.domain.UserStatus;

public class UserAdminResponse {
    private Long userId;
    private String username;
    private String name;
    private String email;
    private Role role;
    private UserStatus status;

    public UserAdminResponse(Long userId, String username, String name,
                             String email, Role role, UserStatus status) {
        this.userId = userId;
        this.username = username;
        this.name = name;
        this.email = email;
        this.role = role;
        this.status = status;
    }

    public static UserAdminResponse fromEntity(User user) {
        return new UserAdminResponse(
                user.getUserId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()
        );
    }

    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public UserStatus getStatus() { return status; }
}
