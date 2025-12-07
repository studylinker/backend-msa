// src/main/java/com/study/study/studyschedule/controller/StudyScheduleController.java
package com.study.study.studyschedule.controller;

import com.study.common.security.JwtUserInfo; // 🟡 JwtUserInfo 사용
import com.study.study.studyschedule.domain.StudySchedule;
import com.study.study.studyschedule.dto.MyScheduleResponse;
import com.study.study.studyschedule.dto.StudyScheduleRequest;
import com.study.study.studyschedule.dto.StudyScheduleResponse;
import com.study.study.studyschedule.dto.StudyScheduleStatusUpdateRequest;
import com.study.study.studyschedule.service.StudyScheduleService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/study-schedules")
public class StudyScheduleController {

    private final StudyScheduleService service;

    public StudyScheduleController(StudyScheduleService service) {
        this.service = service;
    }

    // ================================
    // 일정 단건 조회 (공통)
    // ================================
    @GetMapping("/{scheduleId}")
    public StudyScheduleResponse getById(@PathVariable Long scheduleId) {
        StudySchedule schedule = service.findById(scheduleId);
        return new StudyScheduleResponse(schedule);
    }

    // ================================
    // 일정 생성 (일반 사용자)
    // ================================
    @PostMapping
    public StudyScheduleResponse create(
            @AuthenticationPrincipal JwtUserInfo user,   // 🟡 JwtUserInfo 적용
            @RequestBody StudyScheduleRequest request
    ) {
        Long ownerId = user.getUserId(); // 🟡 userId 사용
        StudySchedule schedule = service.save(request, ownerId);
        return new StudyScheduleResponse(schedule);
    }

    // ================================
    // 일정 수정 (주인 + 리더)
    // ================================
    @PutMapping("/{scheduleId}")
    public StudyScheduleResponse update(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal JwtUserInfo user,  // 🟡 JwtUserInfo 적용
            @RequestBody StudyScheduleRequest request
    ) {
        Long loginUserId = user.getUserId(); // 🟡 userId 사용
        StudySchedule schedule = service.update(scheduleId, request, loginUserId);
        return new StudyScheduleResponse(schedule);
    }

    // ================================
    // 일정 상태 변경 (리더만)
    // ================================
    @PatchMapping("/{scheduleId}/status")
    public StudyScheduleResponse updateStatus(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal JwtUserInfo user,   // 🟡 JwtUserInfo 적용
            @RequestBody StudyScheduleStatusUpdateRequest request
    ) {
        Long loginUserId = user.getUserId();
        StudySchedule schedule = service.updateStatus(scheduleId, request, loginUserId);
        return new StudyScheduleResponse(schedule);
    }

    // ================================
    // 일정 삭제 (주인 + 리더)
    // ================================
    @DeleteMapping("/{scheduleId}")
    public void delete(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal JwtUserInfo user  // 🟡 JwtUserInfo 적용
    ) {
        Long loginUserId = user.getUserId();
        service.deleteById(scheduleId, loginUserId);
    }

    // ================================
    // 내 일정 전체 조회
    // GET /api/study-schedules/me
    // ================================
    @GetMapping("/me")
    public List<MyScheduleResponse> getMySchedules(
            @AuthenticationPrincipal JwtUserInfo user // 🟡 토큰 파싱 제거 → JwtUserInfo로 바로 읽음
    ) {
        Long userId = user.getUserId(); // 🟡 userId 그대로 사용
        return service.getMySchedules(userId);
    }
}
