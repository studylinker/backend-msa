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
    // ✅ 변경 후 정책:
    //  - "로그인된 아무 사용자"나
    //    path 의 userId 에 대한 매너 점수를 조회할 수 있음
    //  - 더 이상 "본인만 조회" 제한 없음
    // ============================
    @GetMapping("/{userId}")
    public ResponseEntity<?> getMannerScore(
            @PathVariable Long userId,
            @AuthenticationPrincipal JwtUserInfo userInfo
    ) {
        // 로그인 안 된 경우만 막기
        if (userInfo == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        // ❌ [삭제된 부분]
        // Long tokenUserId = userInfo.getUserId();
        //
        // if (!userId.equals(tokenUserId)) {
        //     return ResponseEntity
        //             .status(HttpStatus.FORBIDDEN)
        //             .body("본인의 매너 점수만 조회할 수 있습니다.");
        // }

        // ✅ 이제는 path 의 userId 에 대해 누구나 조회 가능
        MannerScoreResponse response = userService.getMannerScore(userId);
        return ResponseEntity.ok(response);
    }
}
