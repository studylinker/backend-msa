package com.study.user.service;

import com.study.user.domain.User;
import com.study.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MannerScoreService {

    private final UserRepository userRepository;

    @Transactional
    public void updateMannerScore(Long userId, String field, float amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));

        switch (field) {
            case "attendance_score" -> user.addAttendanceScore(amount);
            case "leader_score" -> user.addLeaderScore(amount);
            case "violation_score" -> user.addViolationScore(amount);
            default -> throw new IllegalArgumentException("알 수 없는 매너 점수 항목: " + field);
        }
        // @Transactional + JPA 영속성 컨텍스트 덕분에 별도 save() 안 해도 업데이트 반영
    }
}
