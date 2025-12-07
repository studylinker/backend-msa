package com.study.study.attendance.controller;

import com.study.study.attendance.dto.AttendanceRequest;
import com.study.study.attendance.dto.AttendanceResponse;
import com.study.study.attendance.dto.AttendanceStatusUpdateRequest;
import com.study.study.attendance.service.AttendanceService;

// 🟡 JwtUserInfo 사용
import com.study.common.security.JwtUserInfo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    // 🔹 출석 전체 조회 (관리자 전용)
    @GetMapping
    public ResponseEntity<List<AttendanceResponse>> getAll(
            @AuthenticationPrincipal JwtUserInfo user // 🟡 변경됨
    ) {
        System.out.println("[AttendanceController] GET /api/attendance 호출됨");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Long userId = user.getUserId();   // 🟡 JwtUserInfo 방식
        boolean isAdmin = user.isAdmin(); // 🟡 JwtUserInfo 방식

        System.out.println("[AttendanceController] getAll: userId=" + userId + ", isAdmin=" + isAdmin);

        if (!isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<AttendanceResponse> result = attendanceService.findAll();
        return ResponseEntity.ok(result);
    }

    // 🔹 내 출석 전체 조회
    @GetMapping("/me")
    public ResponseEntity<List<AttendanceResponse>> getMyAttendance(
            @AuthenticationPrincipal JwtUserInfo user // 🟡 변경됨
    ) {
        System.out.println("[AttendanceController] GET /api/attendance/me 진입!");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long loginUserId = user.getUserId(); // 🟡 JwtUserInfo 방식

        List<AttendanceResponse> result = attendanceService.findByUser(loginUserId);
        return ResponseEntity.ok(result);
    }

    // 🔹 특정 사용자 출석 조회 (본인 or 관리자)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AttendanceResponse>> getByUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal JwtUserInfo user // 🟡 변경됨
    ) {
        System.out.println("[AttendanceController] GET /api/attendance/user/" + userId);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long loginUserId = user.getUserId();
        boolean isAdmin = user.isAdmin();

        if (!isAdmin && !loginUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<AttendanceResponse> result = attendanceService.findByUser(userId);
        return ResponseEntity.ok(result);
    }

    // 🔹 스케줄별 출석 조회 (리더만)
    @GetMapping("/schedule/{scheduleId}")
    public ResponseEntity<List<AttendanceResponse>> getBySchedule(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal JwtUserInfo user // 🟡 변경됨
    ) {
        System.out.println("[AttendanceController] GET /api/attendance/schedule/" + scheduleId);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long leaderId = user.getUserId(); // 🟡 JwtUserInfo 방식

        List<AttendanceResponse> result =
                attendanceService.findByScheduleForLeader(scheduleId, leaderId);

        return ResponseEntity.ok(result);
    }

    // 🔹 출석 기록 생성/갱신 (리더만)
    @PostMapping
    public ResponseEntity<AttendanceResponse> recordAttendance(
            @AuthenticationPrincipal JwtUserInfo user, // 🟡 변경됨
            @RequestBody AttendanceRequest request
    ) {
        System.out.println("[AttendanceController] POST /api/attendance");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long leaderId = user.getUserId(); // 🟡 JwtUserInfo 방식

        AttendanceResponse response =
                attendanceService.checkIn(request, leaderId);

        return ResponseEntity.ok(response);
    }

    // 🔹 출석 상태 변경 (리더만)
    @PatchMapping("/{attendanceId}")
    public ResponseEntity<AttendanceResponse> updateStatus(
            @PathVariable Long attendanceId,
            @AuthenticationPrincipal JwtUserInfo user, // 🟡 변경됨
            @RequestBody AttendanceStatusUpdateRequest request
    ) {
        System.out.println("[AttendanceController] PATCH /api/attendance/" + attendanceId);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long leaderId = user.getUserId(); // 🟡 JwtUserInfo 방식

        AttendanceResponse response =
                attendanceService.updateStatus(attendanceId, request, leaderId);

        return ResponseEntity.ok(response);
    }
}
