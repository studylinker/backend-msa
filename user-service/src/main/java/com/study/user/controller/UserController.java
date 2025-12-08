package com.study.user.controller;

import com.study.common.security.JwtUserInfo;
import com.study.user.dto.UserRequest;
import com.study.user.dto.UserResponse;
import com.study.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.study.user.studygroup.domain.StudyGroup;
import com.study.user.studygroup.service.StudyGroupService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;
    // private final StudyGroupService studyGroupService;  // MSA 분리로 주석 처리

    // ============================================================
    // 🔥 프론트 유지: GET /api/users/profile (내 프로필 조회)
    // ============================================================
    @GetMapping("/profile")
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal JwtUserInfo user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        UserResponse profile = service.getProfile(user.getUserId());
        return ResponseEntity.ok(profile);
    }


    // ============================================================
    // 회원가입 - POST /api/users
    // ============================================================
    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody UserRequest request) {
        UserResponse created = service.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }


    // ============================================================
    // 회원 정보 수정 - PUT /api/users/{userId}
    // ============================================================
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long userId,
            @RequestBody UserRequest request,
            @AuthenticationPrincipal JwtUserInfo user
    ) {

        if (user == null || !userId.equals(user.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("본인의 정보만 수정할 수 있습니다.");
        }

        return ResponseEntity.ok(service.update(userId, request));
    }


    // ============================================================
    // 회원 삭제 - DELETE /api/users/{userId}
    // ============================================================
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal JwtUserInfo user
    ) {

        if (user == null || !userId.equals(user.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("본인 계정만 삭제할 수 있습니다.");
        }

        service.deleteById(userId);
        return ResponseEntity.ok("계정이 삭제되었습니다.");
    }


    // ============================================================
    // 내가 가입한 스터디 그룹 조회 (MSA 분리 전 기능 → 유지 but 주석만)
    // ============================================================
    @GetMapping("/{userId}/groups")
    public ResponseEntity<?> getJoinedGroups(
            @PathVariable Long userId,
            @AuthenticationPrincipal JwtUserInfo user
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        Long tokenUserId = user.getUserId();

        if (!userId.equals(tokenUserId)) {
            System.out.println("⚠ Path userId != Token userId → 토큰 기준으로 조회");
        }

        Object[] groups = studyGroupClient.getJoinedGroups(tokenUserId);

        return ResponseEntity.ok(groups);
    }
}