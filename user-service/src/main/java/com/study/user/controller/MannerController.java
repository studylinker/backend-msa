package com.study.user.controller;

import com.study.common.security.JwtUserInfo;
import com.study.user.dto.MannerScoreResponse;
import com.study.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/manners")
@RequiredArgsConstructor
public class MannerController {

    private final UserService userService;

    // ============================
    // GET /api/manners/{userId}
    // 매너 점수 조회
    //
    // - 기존 API 경로 그대로 유지
    // - JWT에서 꺼낸 userId랑 path의 userId 비교해서
    //   본인만 조회 가능하게 만들 수 있음
    // ============================
    @GetMapping("/{userId}")
    public ResponseEntity<?> getMannerScore(
            @PathVariable Long userId,
            @AuthenticationPrincipal JwtUserInfo userInfo
    ) {
        // 로그인 안 된 경우
        if (userInfo == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        Long tokenUserId = userInfo.getUserId();

        // ⚠ 본인만 조회하게 막고 싶으면 이 체크 유지
        if (!userId.equals(tokenUserId)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("본인의 매너 점수만 조회할 수 있습니다.");
        }

        // 실제 조회는 userId 기준 (path 또는 tokenUserId 둘 다 같음)
        MannerScoreResponse response = userService.getMannerScore(userId);
        return ResponseEntity.ok(response);
    }
}