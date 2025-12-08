package com.study.study.studygroup.controller;

import com.study.study.studygroup.service.StudyGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/study")
@RequiredArgsConstructor
public class InternalStudyController {

    private final StudyGroupService studyGroupService;

    /**
     * user-service가 호출하는 내부 전용 API
     * GET /internal/study/users/{userId}/groups
     */
    @GetMapping("/users/{userId}/groups")
    public ResponseEntity<?> getUserGroups(@PathVariable Long userId) {
        return ResponseEntity.ok(studyGroupService.findJoinedGroups(userId));
    }
}