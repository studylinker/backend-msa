package com.study.user.admin.controller;

import com.study.common.security.JwtUserInfo;
import com.study.user.admin.dto.UserAdminResponse;
import com.study.user.domain.User;
import com.study.user.dto.StatusUpdateRequest;
import com.study.user.dto.UserRequest;
import com.study.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    private boolean isAdmin(JwtUserInfo user) {
        return user != null && user.isAdmin();
    }

    // ============================
    // GET /api/admin/users
    // 전체 사용자 목록 (관리자 전용)
    // ============================
    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @AuthenticationPrincipal JwtUserInfo currentUser
    ) {
        System.out.println(">>> [Admin] currentUser = " + currentUser);
        if (currentUser != null) {
            System.out.println(">>> [Admin] username = " + currentUser.getUsername());
//            System.out.println(">>> [Admin] authorities = " + currentUser.getAuthorities());
            System.out.println(">>> [Admin] isAdmin = " + currentUser.isAdmin());
        }

        if (!isAdmin(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("관리자만 전체 사용자 목록을 조회할 수 있습니다.");
        }

        List<UserAdminResponse> response = userService.findAll().stream()
                .map(UserAdminResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(response);
    }

    // ============================
    // PATCH /api/admin/users/{userId}/status
    // 사용자 상태 변경
    // ============================
    @PatchMapping("/{userId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long userId,
            @RequestBody StatusUpdateRequest request,
            @AuthenticationPrincipal JwtUserInfo currentUser
    ) {
        if (!isAdmin(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("관리자만 사용자 상태를 변경할 수 있습니다.");
        }

        try {
            User updated = userService.updateStatus(userId, request.getStatus());
            return ResponseEntity.ok(UserAdminResponse.fromEntity(updated));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // ============================
    // PUT /api/admin/users/{userId}
    // 사용자 정보 수정
    // ============================
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long userId,
            @RequestBody UserRequest request,
            @AuthenticationPrincipal JwtUserInfo currentUser
    ) {
        if (!isAdmin(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("관리자만 사용자 정보를 수정할 수 있습니다.");
        }

        try {
            User updated = userService.update(userId, request);
            return ResponseEntity.ok(UserAdminResponse.fromEntity(updated));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    // ============================
    // DELETE /api/admin/users/{userId}
    // 사용자 삭제
    // ============================
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal JwtUserInfo currentUser
    ) {
        if (!isAdmin(currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("관리자만 사용자를 삭제할 수 있습니다.");
        }

        try {
            userService.deleteById(userId);
            return ResponseEntity.ok("사용자가 성공적으로 삭제되었습니다.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}
