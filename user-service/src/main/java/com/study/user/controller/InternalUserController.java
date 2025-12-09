package com.study.user.controller;

import com.study.user.domain.User;
import com.study.user.dto.UserSummaryResponse;
import com.study.user.dto.UserStatDTO;
import com.study.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/users")   // 내부 서비스 통신 전용
public class InternalUserController {

    private final UserRepository userRepository;

    public InternalUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ==========================
    // 기존 기능: 단일 유저 요약 조회
    // ==========================
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserSummary(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("유저를 찾을 수 없습니다. id=" + userId));

        UserSummaryResponse dto = new UserSummaryResponse(
                user.getUserId(),
                user.getUsername(),
                user.getName()
        );

        return ResponseEntity.ok(dto);
    }

}
