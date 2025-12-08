// user-service
package com.study.user.controller;

import com.study.user.domain.User;
import com.study.user.dto.UserSummaryResponse;
import com.study.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/users")   // 외부 클라이언트 말고, 서비스 간 통신용
public class InternalUserController {

    private final UserRepository userRepository;

    public InternalUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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