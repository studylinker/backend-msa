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

    // ==========================
    // 🔥 신규 추가: 통계 서비스용 전체 유저 + 카테고리 조회
    // ==========================
    @GetMapping("/stats")
    public List<UserStatDTO> getUserStats() {
        return userRepository.findAll().stream()
                .map(u -> new UserStatDTO(
                        u.getUserId(),
                        u.getCategories()   // ★ 반드시 User 엔티티에 categories 필드 있어야 함
                ))
                .toList();
    }
}
