package com.study.study.groupmember.controller;

import com.study.study.groupmember.dto.GroupMemberResponse;
import com.study.study.groupmember.dto.GroupMemberStatusUpdateRequest;
import com.study.study.groupmember.service.GroupMemberService;
import com.study.common.security.JwtUserInfo; // MSA 공통 Principal

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/group-members")
public class GroupMemberController {

    private final GroupMemberService service;

    public GroupMemberController(GroupMemberService service) {
        this.service = service;
    }

    // ============================
    // PATCH /api/group-members/{memberId}
    // 멤버 상태 변경 (관리자만)
    // ============================
    @PatchMapping("/{memberId}")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long memberId,
            @RequestBody GroupMemberStatusUpdateRequest request,
            @AuthenticationPrincipal JwtUserInfo user,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader // 🔥 추가: 토큰 전달
    ) {
        System.out.println(">>> [PATCH] JwtUserInfo role = " + user.getRole());
        System.out.println(">>> [PATCH] JwtUserInfo isAdmin = " + user.isAdmin());

        try {
            boolean isAdmin = user.isAdmin();

            GroupMemberResponse updated =
                    service.updateStatusAsAdmin(memberId, request.getStatus(), isAdmin, authorizationHeader);

            return ResponseEntity.ok(updated);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (SecurityException e) { // 권한 문제
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // ============================
    // DELETE /api/group-members/{memberId}
    // 멤버 삭제 (관리자만)
    // ============================
    @DeleteMapping("/{memberId}")
    public ResponseEntity<?> delete(
            @PathVariable Long memberId,
            @AuthenticationPrincipal JwtUserInfo user
    ) {
        System.out.println(">>> [DELETE] JwtUserInfo role = " + user.getRole());
        System.out.println(">>> [DELETE] JwtUserInfo isAdmin = " + user.isAdmin());

        try {
            boolean isAdmin = user.isAdmin();

            service.deleteByIdAsAdmin(memberId, isAdmin);

            return ResponseEntity.ok("멤버가 삭제되었습니다.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}