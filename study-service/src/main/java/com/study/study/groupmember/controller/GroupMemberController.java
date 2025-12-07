// src/main/java/com/study/study/groupmember/controller/GroupMemberController.java
package com.study.study.groupmember.controller;

import com.study.study.groupmember.dto.GroupMemberResponse;
import com.study.study.groupmember.dto.GroupMemberStatusUpdateRequest;
import com.study.study.groupmember.service.GroupMemberService;

// 🟡 CustomUserDetails 제거 (MSA에서는 서비스별 UserDetails 금지)
// import com.study.service.security.CustomUserDetails;

import com.study.common.security.JwtUserInfo; // 🟡 JwtUserInfo 사용 (MSA 공통 Principal)

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
            @AuthenticationPrincipal JwtUserInfo user // 🟡 변경됨: CustomUserDetails → JwtUserInfo
            // 🟡 변경 이유: MSA에서는 모든 서비스가 같은 인증 모델(JwtUserInfo)을 사용해야 함
    ) {
        try {
            boolean isAdmin = user.isAdmin(); // 🟡 표준 메서드 사용

            GroupMemberResponse updated =
                    service.updateStatusAsAdmin(memberId, request.getStatus(), isAdmin);

            return ResponseEntity.ok(updated);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // ============================
    // DELETE /api/group-members/{memberId}
    // 멤버 삭제 (관리자만)
    // ============================
    @DeleteMapping("/{memberId}")
    public ResponseEntity<?> delete(
            @PathVariable Long memberId,
            @AuthenticationPrincipal JwtUserInfo user // 🟡 변경됨: JwtUserInfo 적용
            // 🟡 변경 이유: 인증 정보를 서비스에서 직접 들고 있지 않고 JWT만으로 판단해야 함
    ) {
        try {
            boolean isAdmin = user.isAdmin(); // 🟡 JwtUserInfo 기반 권한 체크

            service.deleteByIdAsAdmin(memberId, isAdmin);

            return ResponseEntity.ok("멤버가 삭제되었습니다.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
