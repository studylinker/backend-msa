package com.study.study.studygroup.controller;

import com.study.study.studygroup.domain.StudyGroup;
import com.study.study.studygroup.dto.*;
import com.study.study.groupmember.dto.GroupMemberResponse;
import com.study.study.studyschedule.dto.StudyScheduleRequest;
import com.study.study.studyschedule.dto.StudyScheduleResponse;
import com.study.study.studygroup.service.StudyGroupService;

import com.study.common.security.JwtUserInfo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StudyGroupController {

    private final StudyGroupService service;

    public StudyGroupController(StudyGroupService service) {
        this.service = service;
    }

    private boolean isAdmin(JwtUserInfo user) {
        return user != null && user.isAdmin();
    }

    // ============================================
    // 그룹 전체 조회
    // ============================================
    @GetMapping("/study-groups")
    public ResponseEntity<List<StudyGroupResponse>> getAll() {
        List<StudyGroup> groups = service.findAll();
        List<StudyGroupResponse> response = groups.stream()
                .map(StudyGroupResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    // ============================================
    // 그룹 단건 조회
    // ============================================
    @GetMapping("/study-groups/{groupId}")
    public ResponseEntity<?> getById(@PathVariable Long groupId) {
        try {
            StudyGroup group = service.findById(groupId);
            return ResponseEntity.ok(StudyGroupResponse.fromEntity(group));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("스터디 그룹을 찾을 수 없습니다. ID: " + groupId);
        }
    }

    // ============================================
    // 그룹 생성
    // ============================================
    @PostMapping("/study-groups")
    public ResponseEntity<?> create(
            @RequestBody StudyGroupRequest request,
            @AuthenticationPrincipal JwtUserInfo user
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        try {
            Long userId = user.getUserId();
            StudyGroup created = service.createGroup(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(StudyGroupResponse.fromEntity(created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ============================================
    // 그룹 수정
    // ============================================
    @PutMapping("/study-groups/{groupId}")
    public ResponseEntity<?> updateGroup(
            @PathVariable Long groupId,
            @RequestBody StudyGroupRequest request,
            @AuthenticationPrincipal JwtUserInfo currentUser
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            Long requesterId = currentUser.getUserId();
            boolean admin = isAdmin(currentUser);

            StudyGroup updated = service.updateGroup(groupId, request, requesterId, admin);
            return ResponseEntity.ok(StudyGroupResponse.fromEntity(updated));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ============================================
    // 그룹 상태 변경
    // ============================================
    @PatchMapping("/study-groups/{groupId}")
    public ResponseEntity<?> updateGroupStatus(
            @PathVariable Long groupId,
            @RequestBody GroupStatusUpdateRequest dto,
            @AuthenticationPrincipal JwtUserInfo currentUser
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            Long requesterId = currentUser.getUserId();
            boolean admin = isAdmin(currentUser);

            service.updateStatus(groupId, dto.getStatus(), requesterId, admin);
            return ResponseEntity.ok("그룹 상태가 변경되었습니다.");

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ============================================
    // 그룹 삭제
    // ============================================
    @DeleteMapping("/study-groups/{id}")
    public ResponseEntity<?> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUserInfo user
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        try {
            Long requesterId = user.getUserId();
            boolean admin = isAdmin(user);

            service.deleteById(id, requesterId, admin);
            return ResponseEntity.ok("스터디 그룹이 성공적으로 삭제되었습니다.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("삭제할 스터디 그룹이 존재하지 않습니다. ID: " + id);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // ============================================
    // 멤버 조회 (리더 + 일반 멤버)
    // ============================================
    @GetMapping("/study-groups/{groupId}/members")
    public ResponseEntity<?> getGroupMembers(
            @PathVariable Long groupId,
            @AuthenticationPrincipal JwtUserInfo currentUser
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        try {
            Long requesterId = currentUser.getUserId();

            // ⭐ 리더/일반 멤버 정책 적용
            List<GroupMemberResponse> members =
                    service.getGroupMembersVisible(groupId, requesterId);

            return ResponseEntity.ok(members);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("그룹 멤버를 찾을 수 없습니다. groupId: " + groupId);
        }
    }

    @GetMapping("/study-groups/{groupId}/leader")
    public ResponseEntity<?> getGroupLeader(@PathVariable Long groupId) {
        try {
            GroupMemberResponse leader = service.getGroupLeader(groupId);
            return ResponseEntity.ok(leader);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("리더 정보를 찾을 수 없습니다. groupId: " + groupId);
        }
    }

    // ============================================
    // 멤버 관련 승인/거절
    // ============================================
    @PostMapping("/study-groups/{groupId}/members/{userId}/approve")
    public ResponseEntity<?> approveMember(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @AuthenticationPrincipal JwtUserInfo currentUser
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            Long leaderId = currentUser.getUserId();
            service.approveMember(groupId, userId, leaderId);
            return ResponseEntity.ok("회원 가입이 승인되었습니다.");

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/study-groups/{groupId}/members/{userId}/reject")
    public ResponseEntity<?> rejectMember(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @AuthenticationPrincipal JwtUserInfo currentUser
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            Long leaderId = currentUser.getUserId();
            service.rejectMember(groupId, userId, leaderId);
            return ResponseEntity.ok("회원 가입이 거절되었습니다.");

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ============================================
    // 일정 조회
    // ============================================
    @GetMapping("/study-groups/{groupId}/schedules")
    public ResponseEntity<?> getGroupSchedules(@PathVariable Long groupId) {
        try {
            List<StudyScheduleResponse> schedules = service.getGroupSchedules(groupId);
            return ResponseEntity.ok(schedules);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("일정 정보를 찾을 수 없습니다.");
        }
    }

}