package com.study.user.dto;

import com.study.user.domain.Role;
import com.study.user.domain.User;
import com.study.user.domain.UserStatus;

import java.time.LocalDateTime;
import java.util.List;

public class UserResponse {

    private Long userId;
    private String username;
    private String name;
    private String email;
    private Role role;
    private UserStatus status;
    private List<String> interestTags;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Float currentMannerScore;
    private Float attendanceScore;
    private Float leaderScore;
    private Float violationScore;

    public UserResponse(Long userId,
                        String username,
                        String name,
                        String email,
                        Role role,
                        UserStatus status,
                        List<String> interestTags,
                        Double latitude,
                        Double longitude,
                        LocalDateTime createdAt,
                        LocalDateTime updatedAt,
                        Float currentMannerScore,
                        Float attendanceScore,
                        Float leaderScore,
                        Float violationScore) {

        this.userId = userId;
        this.username = username;
        this.name = name;
        this.email = email;
        this.role = role;
        this.status = status;
        this.interestTags = interestTags;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.currentMannerScore = currentMannerScore;
        this.attendanceScore = attendanceScore;
        this.leaderScore = leaderScore;
        this.violationScore = violationScore;
    }

    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getInterestTags(),
                user.getLatitude(),
                user.getLongitude(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getCurrentMannerScore(),
                user.getAttendanceScore(),
                user.getLeaderScore(),
                user.getViolationScore()
        );
    }
    // ===== Getter =====
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }

    public UserStatus getStatus() { return status; }

    public List<String> getInterestTags() { return interestTags; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public Float getCurrentMannerScore() { return currentMannerScore; }
    public Float getAttendanceScore() { return attendanceScore; }
    public Float getLeaderScore() { return leaderScore; }
    public Float getViolationScore() { return violationScore; }
}