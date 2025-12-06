package com.study.user.dto;

import com.study.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MannerScoreResponse {

    private Long userId;
    private Float currentMannerScore;
    private Float attendanceScore;
    private Float leaderScore;
    private Float violationScore;

    public static MannerScoreResponse fromEntity(User user) {
        return new MannerScoreResponse(
                user.getUserId(),
                user.getCurrentMannerScore(),
                user.getAttendanceScore(),
                user.getLeaderScore(),
                user.getViolationScore()
        );
    }
}